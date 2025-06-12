package com.example.eventhubapi.event;

import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.mapper.EventMapper;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.eventhubapi.location.Location;
import com.example.eventhubapi.location.LocationService;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.LocationRepository;
import com.example.eventhubapi.location.exception.LocationNotFoundException;

/**
 * Service class for event-related business logic.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final LocationService locationService;
    private final LocationRepository locationRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository, EventMapper eventMapper, LocationService locationService, LocationRepository locationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
        this.locationService = locationService;
        this.locationRepository = locationRepository;
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
    public EventDto createEvent(EventCreationRequest request, String organizerLogin) {
        User organizer = userRepository.findByLogin(organizerLogin)
                .orElseThrow(() -> new UserNotFoundException("Organizer not found with login: " + organizerLogin));

        Event newEvent = eventMapper.toEntity(request, organizer);

        if (request.getLocationId() != null) {
            Location existingLocation = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + request.getLocationId()));
            newEvent.setLocation(existingLocation);
        } else if (request.getLocation() != null) {
            LocationDto createdLocationDto = locationService.createLocation(request.getLocation());
            Location newLocation = locationRepository.findById(createdLocationDto.getId()).orElseThrow();
            newEvent.setLocation(newLocation);
        }

        Event savedEvent = eventRepository.save(newEvent);
        return eventMapper.toDto(savedEvent);
    }

    @Transactional(readOnly = true)
    public Page<EventDto> getPublicEvents(Pageable pageable) {
        return eventRepository.findPublicEvents(pageable).map(eventMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EventDto> getAllEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findAll(pageable);
        return eventPage.map(eventMapper::toDto);
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
        return eventMapper.toDto(event);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventCreationRequest request, String userLogin) {
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + userLogin));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        authorizeOrganizerOrAdmin(event, user);

        Event updatedEvent = eventMapper.toEntity(request, event.getOrganizer()); // Use original organizer
        updatedEvent.setId(eventId); // Ensure we are updating the correct event

        Event savedEvent = eventRepository.save(updatedEvent);
        return eventMapper.toDto(savedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId, String userLogin) {
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + userLogin));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        authorizeOrganizerOrAdmin(event, user);
        eventRepository.deleteById(eventId);
    }
}