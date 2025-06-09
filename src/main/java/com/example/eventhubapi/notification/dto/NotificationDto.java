package com.example.eventhubapi.notification.dto;

import com.example.eventhubapi.common.dto.UserSummary;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationDto {
    private Long id;
    private String message;
    private Instant createdAt;
    private String status;
    private Long eventId;
    private String eventName; // To be populated in the mapper
    private UserSummary recipient;
}