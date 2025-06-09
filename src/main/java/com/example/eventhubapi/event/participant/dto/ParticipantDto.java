package com.example.eventhubapi.event.participant.dto;

import com.example.eventhubapi.common.dto.UserSummary;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantDto {
    private Long id;
    private UserSummary user;
    private String eventRole;
}