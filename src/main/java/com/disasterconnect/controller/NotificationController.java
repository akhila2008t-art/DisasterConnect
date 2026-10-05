package com.disasterconnect.controller;

import com.disasterconnect.entity.Notification;
import com.disasterconnect.entity.User;
import com.disasterconnect.service.NotificationService;
import com.disasterconnect.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(
            NotificationService notificationService,
            UserService userService) {

        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            Principal principal) {

        User user = userService
                .findByEmail(principal.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                notificationService.getUserNotifications(user)
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            Principal principal) {

        User user = userService
                .findByEmail(principal.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                notificationService.getUnreadCount(user)
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok().build();
    }
}