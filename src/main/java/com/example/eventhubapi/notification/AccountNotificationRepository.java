package com.example.eventhubapi.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the AccountNotification join entity.
 */
@Repository
public interface AccountNotificationRepository extends JpaRepository<AccountNotification, AccountNotification.AccountNotificationId> {
    /**
     * Finds all notifications for a specific recipient user.
     * @param recipientId The ID of the recipient user.
     * @return A list of AccountNotifications.
     */
    List<AccountNotification> findByRecipientId(Long recipientId);
}