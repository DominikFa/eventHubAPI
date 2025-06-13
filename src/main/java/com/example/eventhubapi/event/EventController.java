package com.example.eventhubapi.event;

import com.example.eventhubapi.common.dto.EventSummary;
import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.participant.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for managing events and related entities like participants and invitations.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;


    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // --- Event Endpoints ---

    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventCreationRequest request, Authentication authentication) {
        EventDto createdEvent = eventService.createEvent(request, authentication.getName());
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<EventSummary>> getPublicEvents(
            Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        Page<EventSummary> events = eventService.getPublicEvents(pageable, name, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<EventSummary>> getAllEvents(Pageable pageable) {
        Page<EventSummary> events = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id) {
        EventDto event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @Valid @RequestBody EventCreationRequest request, Authentication authentication) {
        EventDto updatedEvent = eventService.updateEvent(id, request, authentication.getName());
        return ResponseEntity.ok(updatedEvent);
    }

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
     * @return ResponseEntity with a page of EventDto objects for participated events and HTTP status 200 OK.
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
     * @return ResponseEntity with a page of EventDto objects for created events and HTTP status 200 OK.
     */
    @GetMapping("/my-created")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Page<EventDto>> getMyCreatedEvents(Authentication authentication, Pageable pageable) {
        Page<EventDto> events = eventService.getMyCreatedEvents(authentication.getName(), pageable);
        return ResponseEntity.ok(events);
    }

}