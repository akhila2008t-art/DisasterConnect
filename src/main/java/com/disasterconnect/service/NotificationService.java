package com.disasterconnect.service;

import com.disasterconnect.entity.Notification;
import com.disasterconnect.entity.User;
import com.disasterconnect.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(
            User user,
            String title,
            String message) {

        Notification notification =
                new Notification(user, title, message);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository
                .countByUserAndIsReadFalse(user);
    }

    public void markAsRead(Long notificationId) {

        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }
}