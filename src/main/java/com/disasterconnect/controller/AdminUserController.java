package com.disasterconnect.controller;

import com.disasterconnect.entity.User;
import com.disasterconnect.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {

        List<User> users =
                userRepository.findAllByOrderByIdAsc();

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (User user : users) {

            Map<String, Object> userData =
                    new HashMap<>();

            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());

            result.add(userData);
        }

        return ResponseEntity.ok(result);
    }

    // =========================================================
    // UPDATE USER ROLE
    // =========================================================

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            Authentication authentication) {

        String newRole =
                role == null
                        ? ""
                        : role.trim().toUpperCase();

        if (!newRole.equals("ADMIN")
                && !newRole.equals("COORDINATOR")
                && !newRole.equals("VOLUNTEER")) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Invalid role. Allowed roles: ADMIN, COORDINATOR, VOLUNTEER."
                    ));
        }

        User user =
                userRepository.findById(id)
                        .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Prevent administrator from changing their own role
        if (authentication != null
                && authentication.getName()
                        .equalsIgnoreCase(user.getEmail())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "You cannot change your own role."
                    ));
        }

        user.setRole(newRole);

        User updatedUser =
                userRepository.save(user);

        Map<String, Object> response =
                new HashMap<>();

        response.put("id", updatedUser.getId());
        response.put("name", updatedUser.getName());
        response.put("email", updatedUser.getEmail());
        response.put("role", updatedUser.getRole());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        User user =
                userRepository.findById(id)
                        .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Prevent administrator from deleting themselves
        if (authentication != null
                && authentication.getName()
                        .equalsIgnoreCase(user.getEmail())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "You cannot delete your own administrator account."
                    ));
        }

        userRepository.delete(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User deleted successfully.",
                        "id",
                        id
                )
        );
    }
}