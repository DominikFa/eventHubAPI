package com.example.eventhubapi.invitation;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.invitation.dto.InvitationCreateRequest;
import com.example.eventhubapi.invitation.dto.InvitationDto;
import com.example.eventhubapi.invitation.enums.InvitationStatus;
import com.example.eventhubapi.invitation.exception.InvitationNotFoundException;
import com.example.eventhubapi.invitation.mapper.InvitationMapper;
import com.example.eventhubapi.notification.NotificationService;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final InvitationMapper invitationMapper;
    private final NotificationService notificationService;

    public InvitationService(InvitationRepository invitationRepository, UserRepository userRepository, EventRepository eventRepository, InvitationMapper invitationMapper, NotificationService notificationService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.invitationMapper = invitationMapper;
        this.notificationService = notificationService;
    }

    private void authorizeOrganizerOrAdmin(Event event, User user) {
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("admin"));
        if (!isOrganizer && !isAdmin) {
            throw new AccessDeniedException("You must be the event organizer or an admin to perform this action.");
        }
    }

    @Transactional
    public InvitationDto createInvitation(InvitationCreateRequest request, String invitingUserLogin) {
        User invitingUser = findUserByLogin(invitingUserLogin);
        User invitedUser = userRepository.findById(request.getInvitedUserId())
                .orElseThrow(() -> new UserNotFoundException("Invited user not found with id: " + request.getInvitedUserId()));
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + request.getEventId()));

        authorizeOrganizerOrAdmin(event, invitingUser);

        Invitation invitation = new Invitation();
        invitation.setInvitedUser(invitedUser);
        invitation.setEvent(event);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setSentAt(Instant.now());

        Invitation savedInvitation = invitationRepository.save(invitation);

        // Create a notification for the invited user
        String notificationMessage = "You have been invited to the event: " + event.getName();
        notificationService.createAndSendNotification(invitedUser, notificationMessage, event.getId());

        return invitationMapper.toDto(savedInvitation);
    }

    @Transactional
    public InvitationDto revokeInvitation(Long invitationId, String userLogin) {
        User user = findUserByLogin(userLogin);
        Invitation invitation = findInvitationById(invitationId);

        authorizeOrganizerOrAdmin(invitation.getEvent(), user);

        if(invitation.getStatus() != InvitationStatus.SENT) {
            throw new IllegalStateException("Only sent invitations can be revoked.");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitation.setRespondedAt(Instant.now());

        Invitation savedInvitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(savedInvitation);
    }

    @Transactional(readOnly = true)
    public Page<InvitationDto> getInvitationsForUser(String userLogin, Pageable pageable) {
        User user = findUserByLogin(userLogin);
        return invitationRepository.findByInvitedUserId(user.getId(), pageable)
                .map(invitationMapper::toDto);
    }

    @Transactional
    public InvitationDto acceptInvitation(Long invitationId, String userLogin) {
        Invitation invitation = findInvitationById(invitationId);
        User user = findUserByLogin(userLogin);
        validateUserIsInvited(invitation, user);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(Instant.now());

        Invitation savedInvitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(savedInvitation);
    }

    @Transactional
    public InvitationDto declineInvitation(Long invitationId, String userLogin) {
        Invitation invitation = findInvitationById(invitationId);
        User user = findUserByLogin(userLogin);
        validateUserIsInvited(invitation, user);

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(Instant.now());

        Invitation savedInvitation = invitationRepository.save(invitation);
        return invitationMapper.toDto(savedInvitation);
    }

    private User findUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + login));
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