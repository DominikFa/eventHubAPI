package com.example.eventhubapi.event;

import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.mapper.EventMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for event-related business logic.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, UserRepository userRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
    }

    /**
     * Creates a new event.
     *
     * @param request DTO with event data.
     * @param organizerLogin Login of the user creating the event.
     * @return DTO of the created event.
     */
    @Transactional
    public EventDto createEvent(EventCreationRequest request, String organizerLogin) {
        User organizer = userRepository.findByLogin(organizerLogin)
                .orElseThrow(() -> new UserNotFoundException("Organizer not found with login: " + organizerLogin));

        Event newEvent = eventMapper.toEntity(request, organizer);
        Event savedEvent = eventRepository.save(newEvent);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Retrieves all events.
     *
     * @return A list of event DTOs.
     */
    @Transactional(readOnly = true)
    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single event by its ID.
     *
     * @param eventId The ID of the event.
     * @return The event DTO.
     */
    @Transactional(readOnly = true)
    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
        return eventMapper.toDto(event);
    }
}