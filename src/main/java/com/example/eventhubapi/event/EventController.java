package com.example.eventhubapi.event;

import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * REST controller for managing events.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Creates a new event.
     * Accessible only by users with EVENT_CREATOR or ADMIN roles.
     *
     * @param request DTO containing event details.
     * @param authentication The current user's authentication details.
     * @return The created event DTO.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventCreationRequest request, Authentication authentication) {
        EventDto createdEvent = eventService.createEvent(request, authentication.getName());
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    /**
     * Retrieves all events.
     *
     * @return A list of all event DTOs.
     */
    @GetMapping
    public ResponseEntity<Page<EventDto>> getAllEvents(Pageable pageable) {
        Page<EventDto> events = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a single event by its ID.
     *
     * @param eventId The ID of the event.
     * @return The event DTO.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) {
        EventDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

}