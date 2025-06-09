package com.example.eventhubapi.event.participant.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.participant.Participant;
import com.example.eventhubapi.event.participant.dto.ParticipantDto;
import org.springframework.stereotype.Service;

@Service
public class ParticipantMapper {
    public ParticipantDto toDto(Participant participant) {
        if (participant == null) return null;

        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getId());
        dto.setEventRole(participant.getEventRole().name());

        if (participant.getUser() != null) {
            dto.setUser(new UserSummary(
                    participant.getUser().getId(),
                    participant.getUser().getName(),
                    participant.getUser().getProfileImageUrl()
            ));
        }

        return dto;
    }
}