package com.example.eventhubapi.notification;

import com.example.eventhubapi.notification.dto.NotificationDto;
import com.example.eventhubapi.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(Authentication authentication, Pageable pageable) {
        Page<NotificationDto> notifications = notificationService.getNotificationsForUser(authentication.getName(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationDto> updateNotificationStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, Authentication authentication) {
        String status = payload.get("status");
        NotificationDto notification = notificationService.updateStatus(id, NotificationStatus.valueOf(status.toUpperCase()), authentication.getName());
        return ResponseEntity.ok(notification);
    }
}