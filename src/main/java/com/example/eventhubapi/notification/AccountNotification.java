package com.example.eventhubapi.notification;

import com.example.eventhubapi.notification.enums.NotificationStatus;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join table entity that links a Notification to a specific User (Account).
 * It tracks the delivery status for each user.
 */
@Entity
@Table(name = "account_notification")
@Getter
@Setter
@NoArgsConstructor
public class AccountNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NotificationStatus status;
}