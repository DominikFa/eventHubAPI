package com.example.eventhubapi.invitation;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.invitation.dto.InvitationCreateRequest;
import com.example.eventhubapi.invitation.dto.InvitationDto;
import com.example.eventhubapi.invitation.enums.InvitationStatus;
import com.example.eventhubapi.invitation.exception.InvitationNotFoundException;
import com.example.eventhubapi.invitation.mapper.InvitationMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final InvitationMapper invitationMapper;

    public InvitationService(InvitationRepository invitationRepository, UserRepository userRepository, EventRepository eventRepository, InvitationMapper invitationMapper) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.invitationMapper = invitationMapper;
    }

    @Transactional
    public InvitationDto createInvitation(InvitationCreateRequest request, String invitingUserEmail) {
        User invitingUser = findUserByEmail(invitingUserEmail);
        User invitedUser = userRepository.findById(request.getInvitedUserId())
                .orElseThrow(() -> new UserNotFoundException("Invited user not found with id: " + request.getInvitedUserId()));
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + request.getEventId()));

        Invitation invitation = new Invitation();
        invitation.setInvitingUser(invitingUser);
        invitation.setInvitedUser(invitedUser);
        invitation.setEvent(event);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setSentAt(Instant.now());

        Invitation savedInvitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(savedInvitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationDto> getInvitationsForUser(String userEmail) {
        User user = findUserByEmail(userEmail);
        return invitationRepository.findByInvitedUserId(user.getId()).stream()
                .map(invitationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvitationDto acceptInvitation(Long invitationId, String userEmail) {
        Invitation invitation = findInvitationById(invitationId);
        User user = findUserByEmail(userEmail);
        validateUserIsInvited(invitation, user);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(Instant.now());

        // Here you would also add the user to the event's participant list
        // eventService.addParticipant(invitation.getEvent().getId(), user.getId());

        Invitation savedInvitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(savedInvitation);
    }

    @Transactional
    public InvitationDto declineInvitation(Long invitationId, String userEmail) {
        Invitation invitation = findInvitationById(invitationId);
        User user = findUserByEmail(userEmail);
        validateUserIsInvited(invitation, user);

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(Instant.now());

        Invitation savedInvitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(savedInvitation);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private Invitation findInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found with id: " + invitationId));
    }

    private void validateUserIsInvited(Invitation invitation, User user) {
        if (!invitation.getInvitedUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to respond to this invitation.");
        }
    }
}