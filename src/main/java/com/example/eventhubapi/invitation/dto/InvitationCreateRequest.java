package com.example.eventhubapi.invitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationCreateRequest {
    @NotNull
    private Long eventId;
    @NotNull
    private Long invitedUserId;
}