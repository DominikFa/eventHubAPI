package com.example.eventhubapi.event.participant.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.participant.Participant;
import com.example.eventhubapi.event.participant.dto.ParticipantDto;
import com.example.eventhubapi.user.User;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class ParticipantMapper {
    public ParticipantDto toDto(Participant participant) {
        if (participant == null) return null;

        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getUser().getId());
        dto.setEventRole(participant.getEventRole().getValue());
        dto.setStatus(participant.getStatus().getValue());

        User user = participant.getUser();
        if (user != null) {
            String userName = user.getProfile() != null ? user.getProfile().getName() : null;
            String imageUrl = null;
            if (user.getProfile() != null && user.getProfile().getProfileImage() != null) {
                imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/users/").path(String.valueOf(user.getId())).path("/profile-image").toUriString();
            }
            dto.setUser(new UserSummary(user.getId(), userName, imageUrl));
        }

        return dto;
    }
}