package com.disasterconnect.controller;

import com.disasterconnect.entity.Volunteer;
import com.disasterconnect.service.VolunteerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class VolunteerController {

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    // =========================================================
    // REGISTER VOLUNTEER
    // =========================================================

    @PostMapping("/volunteer/register")
    public String registerVolunteer(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String contactNumber,
            @RequestParam String skills,
            @RequestParam String location,
            @RequestParam String availability) {

        Volunteer volunteer = new Volunteer(
                name,
                email,
                contactNumber,
                skills,
                location,
                availability
        );

        volunteerService.registerVolunteer(volunteer);

        return "redirect:/";
    }

    // =========================================================
    // GET ALL VOLUNTEERS
    // =========================================================

    @GetMapping("/api/volunteers")
    @ResponseBody
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {

        return ResponseEntity.ok(
                volunteerService.getAllVolunteers()
        );
    }

    // =========================================================
    // GET VOLUNTEER BY ID
    // =========================================================

    @GetMapping("/api/volunteers/{id}")
    @ResponseBody
    public ResponseEntity<Volunteer> getVolunteerById(
            @PathVariable Long id) {

        Optional<Volunteer> volunteer =
                volunteerService.getVolunteerById(id);

        return volunteer
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // GET VOLUNTEER BY EMAIL
    // =========================================================

    @GetMapping("/api/volunteers/email")
    @ResponseBody
    public ResponseEntity<Volunteer> getVolunteerByEmail(
            @RequestParam String email) {

        Optional<Volunteer> volunteer =
                volunteerService.getVolunteerByEmail(email);

        return volunteer
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // GET VOLUNTEERS BY STATUS
    // =========================================================

    @GetMapping("/api/volunteers/status/{status}")
    @ResponseBody
    public ResponseEntity<List<Volunteer>> getVolunteersByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                volunteerService.getVolunteersByStatus(status)
        );
    }

    // =========================================================
    // GET VOLUNTEERS BY AVAILABILITY
    // =========================================================

    @GetMapping("/api/volunteers/availability/{availability}")
    @ResponseBody
    public ResponseEntity<List<Volunteer>> getVolunteersByAvailability(
            @PathVariable String availability) {

        return ResponseEntity.ok(
                volunteerService.getVolunteersByAvailability(
                        availability
                )
        );
    }

    // =========================================================
    // FIND VOLUNTEERS BY LOCATION
    // =========================================================

    @GetMapping("/api/volunteers/location")
    @ResponseBody
    public ResponseEntity<List<Volunteer>> findByLocation(
            @RequestParam String location) {

        return ResponseEntity.ok(
                volunteerService.findVolunteersByLocation(location)
        );
    }

    // =========================================================
    // MATCH VOLUNTEERS FOR AN EMERGENCY
    // =========================================================

    @GetMapping("/api/volunteers/match")
    @ResponseBody
    public ResponseEntity<List<Volunteer>> findMatchingVolunteers(
            @RequestParam String location,
            @RequestParam(required = false) String skill) {

        List<Volunteer> matchingVolunteers =
                volunteerService.findMatchingVolunteers(
                        location,
                        skill
                );

        return ResponseEntity.ok(matchingVolunteers);
    }

    // =========================================================
    // UPDATE VOLUNTEER STATUS
    // =========================================================

    @PutMapping("/api/volunteers/{id}/status")
    @ResponseBody
    public ResponseEntity<Volunteer> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Optional<Volunteer> volunteer =
                volunteerService.updateStatus(id, status);

        return volunteer
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // UPDATE VOLUNTEER AVAILABILITY
    // =========================================================

    @PutMapping("/api/volunteers/{id}/availability")
    @ResponseBody
    public ResponseEntity<Volunteer> updateAvailability(
            @PathVariable Long id,
            @RequestParam String availability) {

        Optional<Volunteer> volunteer =
                volunteerService.updateAvailability(
                        id,
                        availability
                );

        return volunteer
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // =========================================================
    // DELETE VOLUNTEER
    // =========================================================

    @DeleteMapping("/api/volunteers/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteVolunteer(
            @PathVariable Long id) {

        boolean deleted =
                volunteerService.deleteVolunteer(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}