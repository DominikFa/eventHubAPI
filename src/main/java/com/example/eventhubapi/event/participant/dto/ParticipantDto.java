package com.example.eventhubapi.event.participant.dto;

import com.example.eventhubapi.common.dto.UserSummary;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for exposing event participant data to the client.
 */
@Getter
@Setter
public class ParticipantDto {
    private Long id;
    private UserSummary user;
    private String eventRole;
    private String status;
}