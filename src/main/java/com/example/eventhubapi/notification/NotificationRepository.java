package com.example.eventhubapi.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the core Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}