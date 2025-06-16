package com.example.eventhubapi.invitation.mapper;

import com.example.eventhubapi.common.dto.EventSummary;
import com.example.eventhubapi.invitation.Invitation;
import com.example.eventhubapi.invitation.dto.InvitationDto;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * Service class to map Invitation entities to InvitationDto objects.
 */
@Service
public class InvitationMapper {

    private final UserMapper userMapper;

    public InvitationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public InvitationDto toDto(Invitation invitation) {
        if (invitation == null) return null;

        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setStatus(invitation.getStatus().name());
        dto.setSentAt(invitation.getSentAt());
        dto.setRespondedAt(invitation.getRespondedAt());

        if (invitation.getEvent() != null) {
            dto.setEventSummary(new EventSummary(
                    invitation.getEvent().getId(),
                    invitation.getEvent().getName(),
                    invitation.getEvent().getStartDate(),
                    invitation.getEvent().getEndDate()
            ));
        }

        if (invitation.getInvitedUser() != null) {
            dto.setInvitedUser(userMapper.toUserSummary(invitation.getInvitedUser()));
        }

        dto.setInvitingUser(null);

        return dto;
    }
}