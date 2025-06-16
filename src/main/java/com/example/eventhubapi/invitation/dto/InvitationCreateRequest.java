package com.example.eventhubapi.invitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for capturing the data needed to create a new invitation.
 */
@Getter
@Setter
public class InvitationCreateRequest {
    @NotNull
    private Long eventId;
    @NotNull
    private Long invitedUserId;
}