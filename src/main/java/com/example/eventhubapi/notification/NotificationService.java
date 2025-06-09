package com.example.eventhubapi.notification;

import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.notification.dto.NotificationDto;
import com.example.eventhubapi.notification.enums.NotificationStatus;
import com.example.eventhubapi.notification.exception.NotificationNotFoundException;
import com.example.eventhubapi.notification.mapper.NotificationMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final AccountNotificationRepository accountNotificationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository; // To fetch event details
    private final NotificationMapper notificationMapper;

    public NotificationService(AccountNotificationRepository accountNotificationRepository, UserRepository userRepository, EventRepository eventRepository, NotificationMapper notificationMapper) {
        this.accountNotificationRepository = accountNotificationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Creates and sends a notification to a specific user.
     * This would be called by other services (e.g., InvitationService).
     * @param recipient The user who will receive the notification.
     * @param message The notification message content.
     * @param eventId The optional ID of the event related to the notification.
     */
    @Transactional
    public void createAndSendNotification(User recipient, String message, Long eventId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setEventId(eventId);
        notification.setCreatedAt(Instant.now());

        AccountNotification accountNotification = new AccountNotification();
        accountNotification.setRecipient(recipient);
        accountNotification.setNotification(notification); // This will also save the new Notification due to CascadeType.ALL
        accountNotification.setStatus(NotificationStatus.CREATED);

        accountNotificationRepository.save(accountNotification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(String userEmail) {
        User user = findUserByEmail(userEmail);
        List<AccountNotification> notifications = accountNotificationRepository.findByRecipientId(user.getId());
        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markAsRead(Long accountNotificationId, String userEmail) {
        AccountNotification notification = findAccountNotificationById(accountNotificationId);
        User user = findUserByEmail(userEmail);
        validateUserIsRecipient(notification, user);

        notification.setStatus(NotificationStatus.READ);
        AccountNotification savedNotification = accountNotificationRepository.save(notification);
        return notificationMapper.toDto(savedNotification);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private AccountNotification findAccountNotificationById(Long id) {
        return accountNotificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
    }

    private void validateUserIsRecipient(AccountNotification notification, User user) {
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to view or modify this notification.");
        }
    }
}