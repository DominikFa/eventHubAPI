package com.example.eventhubapi.notification;

import com.example.eventhubapi.notification.dto.NotificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for users to manage their notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieves all notifications for the currently authenticated user.
     * @param authentication The current user.
     * @return A list of notification DTOs.
     */
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(Authentication authentication) {
        List<NotificationDto> notifications = notificationService.getNotificationsForUser(authentication.getName());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Marks a specific notification as read.
     * @param notificationId The ID of the AccountNotification to mark as read.
     * @param authentication The current user.
     * @return The updated notification DTO.
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        NotificationDto notification = notificationService.markAsRead(notificationId, authentication.getName());
        return ResponseEntity.ok(notification);
    }
}