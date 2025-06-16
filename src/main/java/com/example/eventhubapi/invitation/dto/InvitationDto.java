package com.example.eventhubapi.invitation.dto;

import com.example.eventhubapi.common.dto.EventSummary;
import com.example.eventhubapi.common.dto.UserSummary;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for exposing invitation data to the client.
 */
@Getter
@Setter
public class InvitationDto {
    private Long id;
    private EventSummary eventSummary;
    private UserSummary invitedUser;
    private UserSummary invitingUser;
    private String status;
    private Instant sentAt;
    private Instant respondedAt;
}