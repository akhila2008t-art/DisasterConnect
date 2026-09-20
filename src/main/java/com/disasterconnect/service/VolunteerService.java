package com.disasterconnect.service;

import com.disasterconnect.entity.Volunteer;
import com.disasterconnect.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    private static final List<String> VALID_STATUSES = List.of(
            "AVAILABLE",
            "ASSIGNED",
            "INACTIVE"
    );

    private static final List<String> VALID_AVAILABILITY = List.of(
            "AVAILABLE",
            "BUSY",
            "UNAVAILABLE"
    );

    public VolunteerService(
            VolunteerRepository volunteerRepository) {

        this.volunteerRepository = volunteerRepository;
    }

    // =========================================================
    // REGISTER VOLUNTEER
    // =========================================================

    public Volunteer registerVolunteer(Volunteer volunteer) {

        if (volunteer.getStatus() == null ||
                volunteer.getStatus().isBlank()) {

            volunteer.setStatus("AVAILABLE");
        }

        if (volunteer.getAvailability() == null ||
                volunteer.getAvailability().isBlank()) {

            volunteer.setAvailability("AVAILABLE");
        }

        volunteer.setStatus(
                volunteer.getStatus().trim().toUpperCase()
        );

        volunteer.setAvailability(
                volunteer.getAvailability().trim().toUpperCase()
        );

        if (!VALID_STATUSES.contains(volunteer.getStatus())) {
            volunteer.setStatus("AVAILABLE");
        }

        if (!VALID_AVAILABILITY.contains(
                volunteer.getAvailability())) {

            volunteer.setAvailability("AVAILABLE");
        }

        return volunteerRepository.save(volunteer);
    }

    // =========================================================
    // GET ALL VOLUNTEERS
    // =========================================================

    public List<Volunteer> getAllVolunteers() {

        return volunteerRepository.findAll();
    }

    // =========================================================
    // GET VOLUNTEER BY ID
    // =========================================================

    public Optional<Volunteer> getVolunteerById(Long id) {

        return volunteerRepository.findById(id);
    }

    // =========================================================
    // GET VOLUNTEER BY EMAIL
    // =========================================================

    public Optional<Volunteer> getVolunteerByEmail(
            String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return volunteerRepository.findByEmail(
                email.trim().toLowerCase()
        );
    }

    // =========================================================
    // GET VOLUNTEERS BY STATUS
    // =========================================================

    public List<Volunteer> getVolunteersByStatus(
            String status) {

        if (status == null || status.isBlank()) {
            return List.of();
        }

        return volunteerRepository.findByStatus(
                status.trim().toUpperCase()
        );
    }

    // =========================================================
    // GET VOLUNTEERS BY AVAILABILITY
    // =========================================================

    public List<Volunteer> getVolunteersByAvailability(
            String availability) {

        if (availability == null ||
                availability.isBlank()) {

            return List.of();
        }

        return volunteerRepository.findByAvailability(
                availability.trim().toUpperCase()
        );
    }

    // =========================================================
    // FIND VOLUNTEERS BY LOCATION
    // =========================================================

    public List<Volunteer> findVolunteersByLocation(
            String location) {

        if (location == null || location.isBlank()) {
            return List.of();
        }

        return volunteerRepository
                .findByLocationContainingIgnoreCase(
                        location.trim()
                );
    }

    // =========================================================
    // SMART VOLUNTEER MATCHING
    // =========================================================

    public List<Volunteer> findMatchingVolunteers(
            String location,
            String requiredSkill) {

        List<Volunteer> availableVolunteers =
                volunteerRepository.findByStatus("AVAILABLE");

        List<Volunteer> matchingVolunteers =
                new ArrayList<>();

        String requestedLocation =
                location == null
                        ? ""
                        : location.trim().toLowerCase();

        String requestedSkill =
                requiredSkill == null
                        ? ""
                        : requiredSkill.trim().toLowerCase();

        for (Volunteer volunteer :
                availableVolunteers) {

            boolean locationMatches = false;
            boolean skillMatches = false;

            // -------------------------------------------------
            // LOCATION MATCH
            // -------------------------------------------------

            if (!requestedLocation.isBlank()
                    && volunteer.getLocation() != null) {

                locationMatches =
                        volunteer.getLocation()
                                .toLowerCase()
                                .contains(requestedLocation);
            }

            // -------------------------------------------------
            // SKILL MATCH
            // -------------------------------------------------

            if (!requestedSkill.isBlank()
                    && volunteer.getSkills() != null) {

                skillMatches =
                        volunteer.getSkills()
                                .toLowerCase()
                                .contains(requestedSkill);
            }

            // -------------------------------------------------
            // MATCHING RULE
            // -------------------------------------------------

            /*
             * If both location and skill are supplied,
             * both should match.
             *
             * If only one is supplied, that criterion
             * is sufficient.
             */

            if (!requestedLocation.isBlank()
                    && !requestedSkill.isBlank()) {

                if (locationMatches && skillMatches) {
                    matchingVolunteers.add(volunteer);
                }

            } else if (!requestedLocation.isBlank()) {

                if (locationMatches) {
                    matchingVolunteers.add(volunteer);
                }

            } else if (!requestedSkill.isBlank()) {

                if (skillMatches) {
                    matchingVolunteers.add(volunteer);
                }
            }
        }

        return matchingVolunteers;
    }

    // =========================================================
    // UPDATE VOLUNTEER STATUS
    // =========================================================

    public Optional<Volunteer> updateStatus(
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

        Optional<Volunteer> optionalVolunteer =
                volunteerRepository.findById(id);

        if (optionalVolunteer.isEmpty()) {
            return Optional.empty();
        }

        Volunteer volunteer =
                optionalVolunteer.get();

        volunteer.setStatus(newStatus);

        /*
         * Keep availability synchronized when possible.
         */
        if ("INACTIVE".equals(newStatus)) {
            volunteer.setAvailability("UNAVAILABLE");
        }

        if ("AVAILABLE".equals(newStatus)) {
            volunteer.setAvailability("AVAILABLE");
        }

        return Optional.of(
                volunteerRepository.save(volunteer)
        );
    }

    // =========================================================
    // UPDATE VOLUNTEER AVAILABILITY
    // =========================================================

    public Optional<Volunteer> updateAvailability(
            Long id,
            String availability) {

        if (availability == null ||
                availability.isBlank()) {

            return Optional.empty();
        }

        String newAvailability =
                availability.trim().toUpperCase();

        if (!VALID_AVAILABILITY.contains(
                newAvailability)) {

            return Optional.empty();
        }

        Optional<Volunteer> optionalVolunteer =
                volunteerRepository.findById(id);

        if (optionalVolunteer.isEmpty()) {
            return Optional.empty();
        }

        Volunteer volunteer =
                optionalVolunteer.get();

        volunteer.setAvailability(newAvailability);

        /*
         * A volunteer marked unavailable should not remain
         * in the normal AVAILABLE state.
         */
        if ("UNAVAILABLE".equals(newAvailability)) {
            volunteer.setStatus("INACTIVE");
        }

        if ("AVAILABLE".equals(newAvailability)
                && !"ASSIGNED".equalsIgnoreCase(
                        volunteer.getStatus())) {

            volunteer.setStatus("AVAILABLE");
        }

        return Optional.of(
                volunteerRepository.save(volunteer)
        );
    }

    // =========================================================
    // DELETE VOLUNTEER
    // =========================================================

    public boolean deleteVolunteer(Long id) {

        Optional<Volunteer> optionalVolunteer =
                volunteerRepository.findById(id);

        if (optionalVolunteer.isEmpty()) {
            return false;
        }

        Volunteer volunteer =
                optionalVolunteer.get();

        /*
         * Do not delete a volunteer who is currently
         * assigned to an emergency.
         */
        if ("ASSIGNED".equalsIgnoreCase(
                volunteer.getStatus())) {

            return false;
        }

        volunteerRepository.delete(volunteer);

        return true;
    }
}