package com.example.eventhubapi.notification;

import com.example.eventhubapi.notification.dto.NotificationDto;
import com.example.eventhubapi.notification.enums.NotificationStatus;
import com.example.eventhubapi.notification.exception.NotificationNotFoundException;
import com.example.eventhubapi.notification.mapper.NotificationMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service class for handling notification-related business logic.
 */
@Service
public class NotificationService {

    private final AccountNotificationRepository accountNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Constructs a NotificationService with the necessary dependencies.
     * @param accountNotificationRepository The repository for the join table between accounts and notifications.
     * @param notificationRepository The repository for notification data access.
     * @param userRepository The repository for user data access.
     * @param notificationMapper The mapper for converting Notification entities to DTOs.
     */
    public NotificationService(AccountNotificationRepository accountNotificationRepository,
                               NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               NotificationMapper notificationMapper) {
        this.accountNotificationRepository = accountNotificationRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Creates and sends a notification to a specific user.
     * @param recipient The user who will receive the notification.
     * @param message The content of the notification message.
     * @param eventId The ID of the event related to the notification, can be null.
     */
    @Transactional
    public void createAndSendNotification(User recipient, String message, Long eventId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setEventId(eventId);
        notification.setCreatedAt(Instant.now());
        Notification savedNotification = notificationRepository.save(notification);

        AccountNotification accountNotification = new AccountNotification();
        accountNotification.setRecipient(recipient);
        accountNotification.setNotification(savedNotification);
        accountNotification.setStatus(NotificationStatus.CREATED);

        accountNotificationRepository.save(accountNotification);
    }

    /**
     * Retrieves a paginated list of notifications for a specific user.
     * @param userLogin The login of the user.
     * @param pageable Pagination and sorting information.
     * @return A Page of NotificationDto objects.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsForUser(String userLogin, Pageable pageable) {
        User user = findUserByLogin(userLogin);
        return accountNotificationRepository.findByRecipientId(user.getId(), pageable)
                .map(notificationMapper::toDto);
    }

    /**
     * Updates the status of a notification for a user.
     * @param notificationId The ID of the notification to update.
     * @param status The new status for the notification.
     * @param userLogin The login of the user whose notification is being updated.
     * @return A NotificationDto representing the updated notification.
     */
    @Transactional
    public NotificationDto updateStatus(Long notificationId, NotificationStatus status, String userLogin) {
        User user = findUserByLogin(userLogin);
        AccountNotification.AccountNotificationId id = new AccountNotification.AccountNotificationId();
        id.setRecipient(user.getId());
        id.setNotification(notificationId);

        AccountNotification notification = accountNotificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + notificationId));

        notification.setStatus(status);
        AccountNotification savedNotification = accountNotificationRepository.save(notification);
        return notificationMapper.toDto(savedNotification);
    }

    private User findUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + login));
    }
}