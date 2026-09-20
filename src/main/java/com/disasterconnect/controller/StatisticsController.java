package com.disasterconnect.controller;

import com.disasterconnect.repository.EmergencyRequestRepository;
import com.disasterconnect.repository.ResourceRepository;
import com.disasterconnect.repository.VolunteerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatisticsController {

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final VolunteerRepository volunteerRepository;
    private final ResourceRepository resourceRepository;

    public StatisticsController(
            EmergencyRequestRepository emergencyRequestRepository,
            VolunteerRepository volunteerRepository,
            ResourceRepository resourceRepository) {

        this.emergencyRequestRepository =
                emergencyRequestRepository;

        this.volunteerRepository =
                volunteerRepository;

        this.resourceRepository =
                resourceRepository;
    }

    @GetMapping("/api/statistics")
    public Map<String, Object> getStatistics() {

        long activeEmergencies =
                emergencyRequestRepository.countByStatusIn(
                        java.util.List.of(
                                "PENDING",
                                "ACCEPTED",
                                "ASSIGNING",
                                "ACTIVE"
                        )
                );

        long availableVolunteers =
                volunteerRepository.countByStatus("AVAILABLE");

        long resources =
                resourceRepository.count();

        long totalEmergencies =
                emergencyRequestRepository.count();

        long completedEmergencies =
                emergencyRequestRepository.countByStatus(
                        "COMPLETED"
                );

        double responseRate = totalEmergencies == 0
                ? 0
                : ((double) completedEmergencies
                        / totalEmergencies) * 100;

        return Map.of(
                "activeEmergencies",
                activeEmergencies,

                "availableVolunteers",
                availableVolunteers,

                "resources",
                resources,

                "responseRate",
                Math.round(responseRate)
        );
    }
}