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

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipantMapper participantMapper;

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

    @Transactional
    public void leaveEvent(Long eventId, String userLogin) {
        User user = findUserByLogin(userLogin);
        Participant participant = participantRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new NotParticipantException("User is not a participant in this event."));

        if (participant.getEventRole() == EventRole.ORGANIZER) {
            throw new IllegalStateException("The organizer cannot leave the event.");
        }

        // Directly delete the participant entity
        participantRepository.delete(participant);
    }

    @Transactional(readOnly = true)
    public Page<ParticipantDto> getParticipantsForEvent(Long eventId, Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
        return participantRepository.findByEventId(eventId, pageable)
                .map(participantMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Map<String, String> getParticipantStatus(Long eventId, String userLogin) {
        User user = findUserByLogin(userLogin);
        return checkStatus(eventId, user.getId());
    }

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