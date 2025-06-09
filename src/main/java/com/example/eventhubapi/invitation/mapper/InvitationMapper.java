package com.example.eventhubapi.invitation.mapper;

import com.example.eventhubapi.common.dto.EventSummary;
import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.invitation.Invitation;
import com.example.eventhubapi.invitation.dto.InvitationDto;
import org.springframework.stereotype.Service;

@Service
public class InvitationMapper {

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
            dto.setInvitedUser(new UserSummary(
                    invitation.getInvitedUser().getId(),
                    invitation.getInvitedUser().getName(),
                    invitation.getInvitedUser().getProfileImageUrl()
            ));
        }

        if (invitation.getInvitingUser() != null) {
            dto.setInvitingUser(new UserSummary(
                    invitation.getInvitingUser().getId(),
                    invitation.getInvitingUser().getName(),
                    invitation.getInvitingUser().getProfileImageUrl()
            ));
        }

        return dto;
    }
}