package com.example.eventhubapi.event;

import com.example.eventhubapi.common.dto.EventSummary;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


/**
 * REST controller for managing events.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    /**
     * Constructs an EventController with the necessary EventService.
     * @param eventService The service for event-related business logic.
     */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Creates a new event.
     * Accessible by users with 'organizer' or 'admin' authority.
     * @param request The request body containing the details of the event to be created.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity containing the created EventDto and HTTP status 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventCreationRequest request, Authentication authentication) {
        EventDto createdEvent = eventService.createEvent(request, authentication.getName());
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of public events, with optional filters.
     * @param pageable Pagination and sorting information.
     * @param name Optional filter for the event name.
     * @param startDate Optional filter for the event start date.
     * @param endDate Optional filter for the event end date.
     * @return A ResponseEntity with a page of EventSummary objects.
     */
    @GetMapping("/public")
    public ResponseEntity<Page<EventSummary>> getPublicEvents(
            Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        Page<EventSummary> events = eventService.getPublicEvents(pageable, name, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a paginated list of all events. (Admin only)
     * @param pageable Pagination and sorting information.
     * @return A ResponseEntity with a page of EventSummary objects.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<EventSummary>> getAllEvents(Pageable pageable) {
        Page<EventSummary> events = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a single event by its ID.
     * @param id The ID of the event to retrieve.
     * @return A ResponseEntity with the EventDto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id) {
        EventDto event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    /**
     * Updates an existing event.
     * Accessible by users with 'organizer' or 'admin' authority.
     * @param id The ID of the event to update.
     * @param request The request body containing the updated event details.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the updated EventDto.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @Valid @RequestBody EventCreationRequest request, Authentication authentication) {
        EventDto updatedEvent = eventService.updateEvent(id, request, authentication.getName());
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Deletes an event.
     * Accessible by users with 'organizer' or 'admin' authority.
     * @param id The ID of the event to delete.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Authentication authentication) {
        eventService.deleteEvent(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a paginated list of events that the current authenticated user is participating in.
     * @param authentication Authentication object containing current user details.
     * @param pageable Pagination information.
     * @return ResponseEntity with a page of EventDto objects for participated events.
     */
    @GetMapping("/my-participated")
    public ResponseEntity<Page<EventDto>> getMyParticipatedEvents(Authentication authentication, Pageable pageable) {
        Page<EventDto> events = eventService.getMyParticipatedEvents(authentication.getName(), pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a paginated list of events that the current authenticated user has created.
     * Accessible by users with 'organizer' or 'admin' authority.
     * @param authentication Authentication object containing current user details.
     * @param pageable Pagination information.
     * @return ResponseEntity with a page of EventDto objects for created events.
     */
    @GetMapping("/my-created")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Page<EventDto>> getMyCreatedEvents(Authentication authentication, Pageable pageable) {
        Page<EventDto> events = eventService.getMyCreatedEvents(authentication.getName(), pageable);
        return ResponseEntity.ok(events);
    }
}