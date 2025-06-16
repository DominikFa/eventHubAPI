package com.example.eventhubapi.event;

import com.example.eventhubapi.common.dto.EventSummary;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.exception.EventNotFoundException;
import com.example.eventhubapi.event.mapper.EventMapper;
import com.example.eventhubapi.event.participant.Participant;
import com.example.eventhubapi.event.participant.ParticipantRepository;
import com.example.eventhubapi.event.participant.enums.EventRole;
import com.example.eventhubapi.event.participant.enums.ParticipantStatus;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.eventhubapi.location.Location;
import com.example.eventhubapi.location.LocationService;
import com.example.eventhubapi.location.dto.LocationDto;
import com.example.eventhubapi.location.LocationRepository;
import com.example.eventhubapi.location.exception.LocationNotFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

/**
 * Service class for event-related business logic.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final EventMapper eventMapper;
    private final LocationService locationService;
    private final LocationRepository locationRepository;

    /**
     * Constructs an EventService with the necessary dependencies.
     *
     * @param eventRepository       The repository for event data access.
     * @param userRepository        The repository for user data access.
     * @param participantRepository The repository for participant data access.
     * @param eventMapper           The mapper for converting between Event entities and DTOs.
     * @param locationService       The service for location-related business logic.
     * @param locationRepository    The repository for location data access.
     */
    public EventService(EventRepository eventRepository, UserRepository userRepository, ParticipantRepository participantRepository, EventMapper eventMapper, LocationService locationService, LocationRepository locationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.eventMapper = eventMapper;
        this.locationService = locationService;
        this.locationRepository = locationRepository;
    }

    private void authorizeOrganizerOrAdmin(Event event, User user) {
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("admin"));
        if (!isOrganizer && !isAdmin) {
            throw new AccessDeniedException("Access Denied");
        }
    }

    /**
     * Creates a new event and sets the organizer as the first participant.
     *
     * @param request        The request object containing event creation data.
     * @param organizerLogin The login of the user creating the event.
     * @return An EventDto representing the newly created event.
     */
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

        Participant organizerParticipant = new Participant();
        organizerParticipant.setUser(organizer);
        organizerParticipant.setEvent(savedEvent);
        organizerParticipant.setEventRole(EventRole.ORGANIZER);
        organizerParticipant.setStatus(ParticipantStatus.ATTENDING);

        participantRepository.save(organizerParticipant);
        savedEvent.getParticipants().add(organizerParticipant);

        return eventMapper.toDto(savedEvent);
    }

    /**
     * Retrieves a paginated list of public events, optionally filtered by name and date range.
     *
     * @param pageable  Pagination information.
     * @param name      Optional filter for event name (case-insensitive, contains).
     * @param startDate Optional filter for events starting on or after this date.
     * @param endDate   Optional filter for events ending on or before this date.
     * @return A Page of EventSummary objects.
     */
    @Transactional(readOnly = true)
    public Page<EventSummary> getPublicEvents(Pageable pageable, String name, Instant startDate, Instant endDate) {
        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("isPublic")));

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return eventRepository.findAll(spec, pageable)
                .map(event -> new EventSummary(event.getId(), event.getName(), event.getStartDate(), event.getEndDate()));
    }

    /**
     * Retrieves a paginated list of all events as summaries. (Admin only)
     *
     * @param pageable Pagination information.
     * @return A Page of EventSummary objects.
     */
    @Transactional(readOnly = true)
    public Page<EventSummary> getAllEvents(Pageable pageable) {
        return eventRepository.findAllSummary(pageable);
    }

    /**
     * Retrieves a single event by its ID.
     *
     * @param eventId The ID of the event to retrieve.
     * @return An EventDto representing the event.
     */
    @Transactional(readOnly = true)
    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
        return eventMapper.toDto(event);
    }

    /**
     * Updates an existing event.
     *
     * @param eventId    The ID of the event to update.
     * @param request    The request object containing the updated event data.
     * @param userLogin  The login of the user performing the update.
     * @return An EventDto representing the updated event.
     */
    @Transactional
    public EventDto updateEvent(Long eventId, EventCreationRequest request, String userLogin) {
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + userLogin));
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        authorizeOrganizerOrAdmin(eventToUpdate, user);

        eventToUpdate.setName(request.getName());
        eventToUpdate.setDescription(request.getDescription());
        eventToUpdate.setStartDate(request.getStartDate());
        eventToUpdate.setEndDate(request.getEndDate());
        eventToUpdate.setPublic(request.getIsPublic());
        eventToUpdate.setMaxParticipants(request.getMaxParticipants());

        Event savedEvent = eventRepository.save(eventToUpdate);
        return eventMapper.toDto(savedEvent);
    }

    /**
     * Deletes an event.
     *
     * @param eventId   The ID of the event to delete.
     * @param userLogin The login of the user performing the deletion.
     */
    @Transactional
    public void deleteEvent(Long eventId, String userLogin) {
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + userLogin));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        authorizeOrganizerOrAdmin(event, user);

        eventRepository.delete(event);
    }

    /**
     * Retrieves a paginated list of events that the current authenticated user is participating in.
     *
     * @param userLogin The login of the current user.
     * @param pageable Pagination information.
     * @return A Page of EventDto objects for participated events.
     */
    @Transactional(readOnly = true)
    public Page<EventDto> getMyParticipatedEvents(String userLogin, Pageable pageable) {
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + userLogin));
        return eventRepository.findEventsByParticipantId(user.getId(), pageable)
                .map(eventMapper::toDto);
    }

    /**
     * Retrieves a paginated list of events that the current authenticated user has created.
     *
     * @param userLogin The login of the current user.
     * @param pageable Pagination information.
     * @return A Page of EventDto objects for created events.
     */
    @Transactional(readOnly = true)
    public Page<EventDto> getMyCreatedEvents(String userLogin, Pageable pageable) {
        User user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + userLogin));
        return eventRepository.findByOrganizerId(user.getId(), pageable)
                .map(eventMapper::toDto);
    }
}