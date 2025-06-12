package com.example.eventhubapi.notification;

import com.example.eventhubapi.notification.enums.NotificationStatus;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Join table entity that links a Notification to a specific User (Account).
 * It tracks the delivery status for each user.
 */
@Entity
@Table(name = "account_notification")
@IdClass(AccountNotification.AccountNotificationId.class)
@Getter
@Setter
@NoArgsConstructor
public class AccountNotification {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User recipient;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(length = 30, nullable = false)
    private NotificationStatus status;

    @Getter
    @Setter
    public static class AccountNotificationId implements Serializable {


        private Long recipient;
        private Long notification;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AccountNotificationId that = (AccountNotificationId) o;
            return Objects.equals(recipient, that.recipient) && Objects.equals(notification, that.notification);
        }

        @Override
        public int hashCode() {
            return Objects.hash(recipient, notification);
        }
    }
}