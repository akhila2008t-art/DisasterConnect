package com.disasterconnect.controller;

import com.disasterconnect.entity.EmergencyRequest;
import com.disasterconnect.service.EmergencyRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class EmergencyRequestController {

    private final EmergencyRequestService emergencyRequestService;

    public EmergencyRequestController(
            EmergencyRequestService emergencyRequestService) {

        this.emergencyRequestService = emergencyRequestService;
    }

    // =========================================================
    // SUBMIT EMERGENCY REQUEST
    // =========================================================

    @PostMapping("/emergency-request")
    public String submitEmergencyRequest(
            @RequestParam String requesterName,
            @RequestParam String contactNumber,
            @RequestParam String location,
            @RequestParam String disasterType,
            @RequestParam String description,
            @RequestParam String urgency,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        EmergencyRequest request =
                new EmergencyRequest(
                        requesterName,
                        contactNumber,
                        location,
                        disasterType,
                        description,
                        urgency,
                        "PENDING"
                );

        // Save GPS coordinates when available
        request.setLatitude(latitude);
        request.setLongitude(longitude);

        emergencyRequestService.createRequest(request);

        return "redirect:/";
    }

    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @GetMapping("/api/emergency-requests")
    @ResponseBody
    public ResponseEntity<List<EmergencyRequest>> getAllRequests() {

        return ResponseEntity.ok(
                emergencyRequestService.getAllRequests()
        );
    }

    // =========================================================
    // GET REQUEST BY ID
    // =========================================================

    @GetMapping("/api/emergency-requests/{id}")
    @ResponseBody
    public ResponseEntity<EmergencyRequest> getRequestById(
            @PathVariable Long id) {

        Optional<EmergencyRequest> request =
                emergencyRequestService.getRequestById(id);

        return request
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // UPDATE EMERGENCY STATUS
    // =========================================================

    @PutMapping("/api/emergency-requests/{id}/status")
    @ResponseBody
    public ResponseEntity<EmergencyRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Optional<EmergencyRequest> updatedRequest =
                emergencyRequestService.updateStatus(
                        id,
                        status
                );

        return updatedRequest
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.badRequest().build()
                );
    }

    // =========================================================
    // UPDATE VOLUNTEER ACTION STATUS
    // =========================================================

    @PutMapping("/api/emergency-requests/{id}/volunteer-status")
    @ResponseBody
    public ResponseEntity<EmergencyRequest> updateVolunteerActionStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Optional<EmergencyRequest> updatedRequest =
                emergencyRequestService.updateVolunteerActionStatus(
                        id,
                        status
                );

        return updatedRequest
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.badRequest().build()
                );
    }

    // =========================================================
    // ASSIGN VOLUNTEER
    // =========================================================

    @PutMapping("/api/emergency-requests/{id}/assign")
    @ResponseBody
    public ResponseEntity<EmergencyRequest> assignVolunteer(
            @PathVariable Long id,
            @RequestParam Long volunteerId) {

        Optional<EmergencyRequest> updatedRequest =
                emergencyRequestService.assignVolunteer(
                        id,
                        volunteerId
                );

        return updatedRequest
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.badRequest().build()
                );
    }

    // =========================================================
    // DELETE REQUEST
    // =========================================================

    @DeleteMapping("/api/emergency-requests/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Long id) {

        boolean deleted =
                emergencyRequestService.deleteRequest(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}