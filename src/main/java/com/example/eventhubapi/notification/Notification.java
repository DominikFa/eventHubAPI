package com.example.eventhubapi.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing a generic notification message.
 * This stores the core content of the notification.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // Optional: A link to the event that triggered this notification
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
}