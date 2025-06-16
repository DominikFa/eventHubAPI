package com.example.eventhubapi.event.participant;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.EventRepository;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.participant.dto.ParticipantDto;
import com.example.eventhubapi.event.participant.enums.EventRole;
import com.example.eventhubapi.event.participant.enums.ParticipantStatus;
import com.example.eventhubapi.event.participant.exception.AlreadyParticipantException;
import com.example.eventhubapi.event.participant.exception.NotParticipantException;
import com.example.eventhubapi.event.participant.mapper.ParticipantMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service class for handling participant-related business logic.
 */
@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipantMapper participantMapper;

    /**
     * Constructs a ParticipantService with the necessary dependencies.
     *
     * @param participantRepository The repository for participant data access.
     * @param eventRepository       The repository for event data access.
     * @param userRepository        The repository for user data access.
     * @param participantMapper     The mapper for converting Participant entities to DTOs.
     */
    public ParticipantService(ParticipantRepository participantRepository, EventRepository eventRepository, UserRepository userRepository, ParticipantMapper participantMapper) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.participantMapper = participantMapper;
    }

    private void authorizeOrganizerOrAdmin(Event event, User user) {
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("admin"));
        if (!isOrganizer && !isAdmin) {
            throw new AccessDeniedException("You must be the event organizer or an admin to perform this action.");
        }
    }

    /**
     * Allows a user to join an event.
     *
     * @param eventId   The ID of the event to join.
     * @param userLogin The login of the user joining.
     * @return A ParticipantDto representing the new participant.
     */
    @Transactional
    public ParticipantDto joinEvent(Long eventId, String userLogin) {
        User user = findUserByLogin(userLogin);
        Event event = findEventByIdWithPessimisticLock(eventId);

        if (participantRepository.findByEventIdAndUserId(eventId, user.getId()).isPresent()) {
            throw new AlreadyParticipantException("User is already a participant in this event.");
        }

        if (event.getMaxParticipants() != null) {
            long attendingCount = participantRepository.countByEventIdAndStatus(eventId, ParticipantStatus.ATTENDING);
            if (attendingCount >= event.getMaxParticipants()) {
                throw new IllegalStateException("Event is full.");
            }
        }

        Participant participant = new Participant();
        participant.setUser(user);
        participant.setEvent(event);
        participant.setEventRole(EventRole.PARTICIPANT);
        participant.setStatus(ParticipantStatus.ATTENDING);
        event.getParticipants().add(participant);

        Participant savedParticipant = participantRepository.save(participant);
        return participantMapper.toDto(savedParticipant);
    }

    /**
     * Allows a user to leave an event.
     *
     * @param eventId   The ID of the event to leave.
     * @param userLogin The login of the user leaving.
     */
    @Transactional
    public void leaveEvent(Long eventId, String userLogin) {
        User user = findUserByLogin(userLogin);
        Participant participant = participantRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new NotParticipantException("User is not a participant in this event."));

        if (participant.getEventRole() == EventRole.ORGANIZER) {
            throw new IllegalStateException("The organizer cannot leave the event.");
        }

        participantRepository.delete(participant);
    }

    /**
     * Retrieves a paginated list of participants for a given event.
     *
     * @param eventId  The ID of the event.
     * @param pageable Pagination information.
     * @return A Page of ParticipantDto objects.
     */
    @Transactional(readOnly = true)
    public Page<ParticipantDto> getParticipantsForEvent(Long eventId, Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
        return participantRepository.findByEventId(eventId, pageable)
                .map(participantMapper::toDto);
    }

    /**
     * Gets the participation status of the current user for an event.
     *
     * @param eventId   The ID of the event.
     * @param userLogin The login of the current user.
     * @return A map containing the user's status.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getParticipantStatus(Long eventId, String userLogin) {
        User user = findUserByLogin(userLogin);
        return checkStatus(eventId, user.getId());
    }

    /**
     * Gets the participation status of a specific user for an event (organizer/admin only).
     *
     * @param eventId        The ID of the event.
     * @param userId         The ID of the user whose status to check.
     * @param organizerLogin The login of the user making the request (must be organizer/admin).
     * @return A map containing the user's status.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getParticipantStatusForUser(Long eventId, Long userId, String organizerLogin) {
        Event event = findEventById(eventId);
        User organizer = findUserByLogin(organizerLogin);
        authorizeOrganizerOrAdmin(event, organizer);

        return checkStatus(eventId, userId);
    }

    private Map<String, String> checkStatus(Long eventId, Long userId) {
        String status = participantRepository.findByEventIdAndUserId(eventId, userId)
                .map(participant -> participant.getStatus().getValue())
                .orElse("not_participant");

        return Map.of("status", status);
    }

    private User findUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + login));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
    }

    private Event findEventByIdWithPessimisticLock(Long eventId) {
        return eventRepository.findByIdWithPessimisticLock(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
    }

    /**
     * Updates the status of a participant in an event (organizer/admin only).
     *
     * @param eventId        The ID of the event.
     * @param userId         The ID of the participant to update.
     * @param newStatus      The new status for the participant.
     * @param currentUserLogin The login of the user making the request.
     * @return A ParticipantDto representing the updated participant.
     */
    @Transactional
    public ParticipantDto updateParticipantStatus(Long eventId, Long userId, String newStatus, String currentUserLogin) {
        User currentUser = findUserByLogin(currentUserLogin);
        Event event = findEventById(eventId);

        authorizeOrganizerOrAdmin(event, currentUser);

        Participant participant = participantRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotParticipantException("User is not a participant in this event."));

        participant.setStatus(ParticipantStatus.fromValue(newStatus));
        Participant updatedParticipant = participantRepository.save(participant);
        return participantMapper.toDto(updatedParticipant);
    }
}