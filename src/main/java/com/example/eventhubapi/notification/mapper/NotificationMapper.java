package com.example.eventhubapi.notification.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.notification.AccountNotification;
import com.example.eventhubapi.notification.Notification;
import com.example.eventhubapi.notification.dto.NotificationDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationMapper {

    private final EventRepository eventRepository;

    public NotificationMapper(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public NotificationDto toDto(AccountNotification accountNotification) {
        if (accountNotification == null) return null;

        Notification notification = accountNotification.getNotification();
        if (notification == null) return null;

        NotificationDto dto = new NotificationDto();
        dto.setId(accountNotification.getId());
        dto.setStatus(accountNotification.getStatus().name());

        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());

        if (notification.getEventId() != null) {
            dto.setEventId(notification.getEventId());
            // Fetch event name to enrich the DTO
            Optional<Event> eventOpt = eventRepository.findById(notification.getEventId());
            eventOpt.ifPresent(event -> dto.setEventName(event.getName()));
        }

        if (accountNotification.getRecipient() != null) {
            dto.setRecipient(new UserSummary(
                    accountNotification.getRecipient().getId(),
                    accountNotification.getRecipient().getName(),
                    accountNotification.getRecipient().getProfileImageUrl()
            ));
        }

        return dto;
    }
}