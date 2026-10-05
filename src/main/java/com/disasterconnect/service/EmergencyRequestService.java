package com.disasterconnect.service;

import com.disasterconnect.entity.EmergencyRequest;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.Volunteer;
import com.disasterconnect.repository.EmergencyRequestRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyRequestService {

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // =========================================================
    // VALID EMERGENCY STATUSES
    // =========================================================

    private static final List<String> VALID_STATUSES = List.of(
            "PENDING",
            "ACCEPTED",
            "IN_PROGRESS",
            "RESOLVED",
            "CANCELLED"
    );

    // =========================================================
    // VALID VOLUNTEER ACTION STATUSES
    // =========================================================

    private static final List<String> VALID_VOLUNTEER_ACTION_STATUSES = List.of(
            "NOT_STARTED",
            "IN_PROGRESS",
            "COMPLETED"
    );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public EmergencyRequestService(
            EmergencyRequestRepository emergencyRequestRepository,
            VolunteerRepository volunteerRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.emergencyRequestRepository = emergencyRequestRepository;
        this.volunteerRepository = volunteerRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

        // Every new emergency starts with volunteer action
        // status NOT_STARTED.
        if (request.getVolunteerActionStatus() == null ||
                request.getVolunteerActionStatus().isBlank()) {

            request.setVolunteerActionStatus("NOT_STARTED");
        }

        request.setVolunteerActionStatus(
                request.getVolunteerActionStatus()
                        .trim()
                        .toUpperCase()
        );

        if (!VALID_VOLUNTEER_ACTION_STATUSES.contains(
                request.getVolunteerActionStatus())) {

            request.setVolunteerActionStatus("NOT_STARTED");
        }

        EmergencyRequest savedRequest =
                emergencyRequestRepository.save(request);

        // =====================================================
        // CREATE NOTIFICATIONS FOR ADMINS AND VOLUNTEERS
        // =====================================================

        List<User> admins =
                userRepository.findByRoleIgnoreCase("ADMIN");

        List<User> volunteers =
                userRepository.findByRoleIgnoreCase("VOLUNTEER");

        String title = "🚨 New Emergency Request";

        String message =
                "A new emergency request has been reported."
                + " Location: " + request.getLocation()
                + ". Disaster type: " + request.getDisasterType()
                + ". Urgency: " + request.getUrgency() + ".";

        for (User admin : admins) {

            notificationService.createNotification(
                    admin,
                    title,
                    message
            );
        }

        for (User volunteer : volunteers) {

            notificationService.createNotification(
                    volunteer,
                    title,
                    message
            );
        }

        return savedRequest;
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

        String newStatus =
                status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(newStatus)) {
            return Optional.empty();
        }

        Optional<EmergencyRequest> optionalRequest =
                emergencyRequestRepository.findById(id);

        if (optionalRequest.isEmpty()) {
            return Optional.empty();
        }

        EmergencyRequest request =
                optionalRequest.get();

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

        EmergencyRequest updatedRequest =
                emergencyRequestRepository.save(request);

        return Optional.of(updatedRequest);
    }

    // =========================================================
    // UPDATE VOLUNTEER ACTION STATUS
    // =========================================================

    public Optional<EmergencyRequest> updateVolunteerActionStatus(
            Long id,
            String volunteerActionStatus) {

        if (volunteerActionStatus == null ||
                volunteerActionStatus.isBlank()) {

            return Optional.empty();
        }

        String newStatus =
                volunteerActionStatus.trim().toUpperCase();

        if (!VALID_VOLUNTEER_ACTION_STATUSES.contains(newStatus)) {
            return Optional.empty();
        }

        Optional<EmergencyRequest> optionalRequest =
                emergencyRequestRepository.findById(id);

        if (optionalRequest.isEmpty()) {
            return Optional.empty();
        }

        EmergencyRequest request =
                optionalRequest.get();

        request.setVolunteerActionStatus(newStatus);

        EmergencyRequest updatedRequest =
                emergencyRequestRepository.save(request);

        return Optional.of(updatedRequest);
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
         * When a new volunteer is assigned,
         * reset the volunteer action status.
         */
        request.setVolunteerActionStatus("NOT_STARTED");

        /*
         * Automatically accept a pending request.
         */
        if ("PENDING".equalsIgnoreCase(
                request.getStatus())) {

            request.setStatus("ACCEPTED");
        }

        newVolunteer.setStatus("ASSIGNED");

        volunteerRepository.save(newVolunteer);

        EmergencyRequest savedRequest =
                emergencyRequestRepository.save(request);

        // =====================================================
        // NOTIFY THE ASSIGNED VOLUNTEER
        // =====================================================

        Optional<User> volunteerUser =
                userRepository.findByEmail(
                        newVolunteer.getEmail()
                );

        if (volunteerUser.isPresent()) {

            notificationService.createNotification(
                    volunteerUser.get(),
                    "🤝 Emergency Assigned",
                    "You have been assigned to an emergency request."
            );
        }

        return Optional.of(savedRequest);
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