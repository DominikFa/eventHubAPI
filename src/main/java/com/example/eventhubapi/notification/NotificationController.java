package com.example.eventhubapi.notification;

import com.example.eventhubapi.notification.dto.NotificationDto;
import com.example.eventhubapi.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing user notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Constructs a NotificationController with the necessary NotificationService.
     * @param notificationService The service for notification-related business logic.
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieves a paginated list of notifications for the currently authenticated user.
     * @param authentication The authentication object of the current user.
     * @param pageable Pagination and sorting information.
     * @return A ResponseEntity with a page of NotificationDto objects.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(Authentication authentication, Pageable pageable) {
        Page<NotificationDto> notifications = notificationService.getNotificationsForUser(authentication.getName(), pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Updates the status of a specific notification for the current user.
     * @param id The ID of the notification to update.
     * @param payload A map containing the new status.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the updated NotificationDto.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationDto> updateNotificationStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, Authentication authentication) {
        String status = payload.get("status");
        NotificationDto notification = notificationService.updateStatus(id, NotificationStatus.valueOf(status.toUpperCase()), authentication.getName());
        return ResponseEntity.ok(notification);
    }
}