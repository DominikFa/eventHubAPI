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

@Service
public class NotificationService {

    private final AccountNotificationRepository accountNotificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(AccountNotificationRepository accountNotificationRepository, UserRepository userRepository, NotificationMapper notificationMapper) {
        this.accountNotificationRepository = accountNotificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public void createAndSendNotification(User recipient, String message, Long eventId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setEventId(eventId);
        notification.setCreatedAt(Instant.now());

        AccountNotification accountNotification = new AccountNotification();
        accountNotification.setRecipient(recipient);
        accountNotification.setNotification(notification);
        accountNotification.setStatus(NotificationStatus.CREATED);

        accountNotificationRepository.save(accountNotification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsForUser(String userLogin, Pageable pageable) {
        User user = findUserByLogin(userLogin);
        return accountNotificationRepository.findByRecipientId(user.getId(), pageable)
                .map(notificationMapper::toDto);
    }

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
