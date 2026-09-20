package com.disasterconnect.service;

import com.disasterconnect.entity.EmergencyRequest;
import com.disasterconnect.entity.Volunteer;
import com.disasterconnect.repository.EmergencyRequestRepository;
import com.disasterconnect.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyRequestService {

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final VolunteerRepository volunteerRepository;

    private static final List<String> VALID_STATUSES = List.of(
            "PENDING",
            "ACCEPTED",
            "IN_PROGRESS",
            "RESOLVED",
            "CANCELLED"
    );

    public EmergencyRequestService(
            EmergencyRequestRepository emergencyRequestRepository,
            VolunteerRepository volunteerRepository) {

        this.emergencyRequestRepository = emergencyRequestRepository;
        this.volunteerRepository = volunteerRepository;
    }

    // =========================================================
    // CREATE A NEW EMERGENCY REQUEST
    // =========================================================

    public EmergencyRequest createRequest(EmergencyRequest request) {

        if (request.getStatus() == null ||
                request.getStatus().isBlank()) {

            request.setStatus("PENDING");
        }

        request.setStatus(
                request.getStatus().trim().toUpperCase()
        );

        if (!VALID_STATUSES.contains(request.getStatus())) {
            request.setStatus("PENDING");
        }

        return emergencyRequestRepository.save(request);
    }

    // =========================================================
    // GET ALL EMERGENCY REQUESTS
    // =========================================================

    public List<EmergencyRequest> getAllRequests() {

        return emergencyRequestRepository.findAll();
    }

    // =========================================================
    // GET EMERGENCY REQUEST BY ID
    // =========================================================

    public Optional<EmergencyRequest> getRequestById(Long id) {

        return emergencyRequestRepository.findById(id);
    }

    // =========================================================
    // UPDATE REQUEST STATUS
    // =========================================================

    public Optional<EmergencyRequest> updateStatus(
            Long id,
            String status) {

        if (status == null || status.isBlank()) {
            return Optional.empty();
        }

        String newStatus = status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(newStatus)) {
            return Optional.empty();
        }

        Optional<EmergencyRequest> optionalRequest =
                emergencyRequestRepository.findById(id);

        if (optionalRequest.isEmpty()) {
            return Optional.empty();
        }

        EmergencyRequest request = optionalRequest.get();

        request.setStatus(newStatus);

        /*
         * If an emergency is resolved or cancelled,
         * release the assigned volunteer.
         */
        if (("RESOLVED".equals(newStatus)
                || "CANCELLED".equals(newStatus))
                && request.getAssignedVolunteerId() != null) {

            Long volunteerId =
                    request.getAssignedVolunteerId();

            Optional<Volunteer> optionalVolunteer =
                    volunteerRepository.findById(volunteerId);

            if (optionalVolunteer.isPresent()) {

                Volunteer volunteer =
                        optionalVolunteer.get();

                volunteer.setStatus("AVAILABLE");

                volunteerRepository.save(volunteer);
            }

            request.setAssignedVolunteerId(null);
        }

        return Optional.of(
                emergencyRequestRepository.save(request)
        );
    }

    // =========================================================
    // ASSIGN VOLUNTEER TO EMERGENCY REQUEST
    // =========================================================

    public Optional<EmergencyRequest> assignVolunteer(
            Long id,
            Long volunteerId) {

        Optional<EmergencyRequest> optionalRequest =
                emergencyRequestRepository.findById(id);

        if (optionalRequest.isEmpty()) {
            return Optional.empty();
        }

        Optional<Volunteer> optionalVolunteer =
                volunteerRepository.findById(volunteerId);

        if (optionalVolunteer.isEmpty()) {
            return Optional.empty();
        }

        EmergencyRequest request =
                optionalRequest.get();

        Volunteer newVolunteer =
                optionalVolunteer.get();

        /*
         * Prevent assigning the same volunteer again.
         */
        if (volunteerId.equals(
                request.getAssignedVolunteerId())) {

            return Optional.of(request);
        }

        /*
         * If another volunteer is already assigned,
         * release that volunteer first.
         */
        if (request.getAssignedVolunteerId() != null) {

            Long previousVolunteerId =
                    request.getAssignedVolunteerId();

            Optional<Volunteer> previousVolunteer =
                    volunteerRepository.findById(
                            previousVolunteerId
                    );

            if (previousVolunteer.isPresent()) {

                Volunteer previous =
                        previousVolunteer.get();

                previous.setStatus("AVAILABLE");

                volunteerRepository.save(previous);
            }
        }

        /*
         * New volunteer must be available.
         */
        if (!"AVAILABLE".equalsIgnoreCase(
                newVolunteer.getStatus())) {

            return Optional.empty();
        }

        request.setAssignedVolunteerId(volunteerId);

        /*
         * Automatically accept a pending request.
         */
        if ("PENDING".equalsIgnoreCase(
                request.getStatus())) {

            request.setStatus("ACCEPTED");
        }

        newVolunteer.setStatus("ASSIGNED");

        volunteerRepository.save(newVolunteer);

        return Optional.of(
                emergencyRequestRepository.save(request)
        );
    }

    // =========================================================
    // DELETE EMERGENCY REQUEST
    // =========================================================

    public boolean deleteRequest(Long id) {

        Optional<EmergencyRequest> optionalRequest =
                emergencyRequestRepository.findById(id);

        if (optionalRequest.isEmpty()) {
            return false;
        }

        EmergencyRequest request =
                optionalRequest.get();

        /*
         * Release assigned volunteer before deletion.
         */
        if (request.getAssignedVolunteerId() != null) {

            Long volunteerId =
                    request.getAssignedVolunteerId();

            Optional<Volunteer> optionalVolunteer =
                    volunteerRepository.findById(volunteerId);

            if (optionalVolunteer.isPresent()) {

                Volunteer volunteer =
                        optionalVolunteer.get();

                volunteer.setStatus("AVAILABLE");

                volunteerRepository.save(volunteer);
            }
        }

        emergencyRequestRepository.delete(request);

        return true;
    }
}