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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.eventhubapi.location.Location;
import com.example.eventhubapi.location.LocationService;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.LocationRepository;
import com.example.eventhubapi.location.exception.LocationNotFoundException;

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
    private final LocationService locationService;
    private final LocationRepository locationRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository, EventMapper eventMapper, LocationService locationService, LocationRepository locationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
        this.locationService = locationService;
        this.locationRepository = locationRepository;
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


        // Case 1: Link to an existing location by its ID
        if (request.getLocationId() != null) {
            Location existingLocation = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + request.getLocationId()));
            newEvent.setLocation(existingLocation);
        }
        // Case 2: Create a new location from the nested object
        else if (request.getLocation() != null) {
            LocationDto createdLocationDto = locationService.createLocation(request.getLocation());
            Location newLocation = locationRepository.findById(createdLocationDto.getId()).get();
            newEvent.setLocation(newLocation);
        }

        Event savedEvent = eventRepository.save(newEvent);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Retrieves all events.
     *
     * @return A list of event DTOs.
     */
    @Transactional(readOnly = true)
    public Page<EventDto> getAllEvents(Pageable pageable) {
        // The repository call now returns a Page<Event>
        Page<Event> eventPage = eventRepository.findAll(pageable);
        // The map function on Page converts its content from Event to EventDto
        return eventPage.map(eventMapper::toDto);
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