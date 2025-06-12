package com.example.eventhubapi.event;

import com.example.eventhubapi.event.dto.EventCreationRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.participant.ParticipantService;
import com.example.eventhubapi.event.participant.dto.ParticipantDto;
import com.example.eventhubapi.invitation.InvitationService;
import com.example.eventhubapi.invitation.dto.InvitationCreateRequest;
import com.example.eventhubapi.invitation.dto.InvitationDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing events and related entities like participants and invitations.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final ParticipantService participantService;
    private final InvitationService invitationService;

    public EventController(EventService eventService, ParticipantService participantService, InvitationService invitationService) {
        this.eventService = eventService;
        this.participantService = participantService;
        this.invitationService = invitationService;
    }

    // --- Event Endpoints ---

    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventCreationRequest request, Authentication authentication) {
        EventDto createdEvent = eventService.createEvent(request, authentication.getName());
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<EventDto>> getPublicEvents(Pageable pageable) {
        Page<EventDto> events = eventService.getPublicEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<EventDto>> getAllEvents(Pageable pageable) {
        Page<EventDto> events = eventService.getAllEvents(pageable);
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

    // --- Participation Endpoints ---

    @PostMapping("/{id}/join")
    public ResponseEntity<ParticipantDto> joinEvent(@PathVariable Long id, Authentication authentication) {
        ParticipantDto participant = participantService.joinEvent(id, authentication.getName());
        return new ResponseEntity<>(participant, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/participant-status")
    public ResponseEntity<Map<String, String>> getParticipantStatus(@PathVariable Long id, Authentication authentication) {
        Map<String, String> status = participantService.getParticipantStatus(id, authentication.getName());
        return ResponseEntity.ok(status);
    }

    // --- Invitation Endpoints ---

    @PostMapping("/{id}/invite")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<InvitationDto> inviteToEvent(@PathVariable Long id, @RequestBody InvitationCreateRequest request, Authentication authentication) {
        request.setEventId(id);
        InvitationDto createdInvitation = invitationService.createInvitation(request, authentication.getName());
        return new ResponseEntity<>(createdInvitation, HttpStatus.CREATED);
    }
}
