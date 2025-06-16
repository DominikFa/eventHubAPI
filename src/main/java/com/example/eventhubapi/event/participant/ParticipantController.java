package com.example.eventhubapi.event.participant;

import com.example.eventhubapi.event.participant.dto.ParticipantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST controller for managing event participants.
 */
@RestController
@RequestMapping("/api/events/{eventId}/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    /**
     * Constructs a ParticipantController with the necessary ParticipantService.
     * @param participantService The service for participant-related business logic.
     */
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * Allows an authenticated user to join an event.
     * @param eventId The ID of the event to join.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the created ParticipantDto and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<ParticipantDto> joinEvent(@PathVariable Long eventId, Authentication authentication) {
        ParticipantDto participant = participantService.joinEvent(eventId, authentication.getName());
        return new ResponseEntity<>(participant, HttpStatus.CREATED);
    }

    /**
     * Retrieves the participation status of the current user for an event.
     * @param eventId The ID of the event.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with a map containing the status.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getParticipantStatus(@PathVariable Long eventId, Authentication authentication) {
        Map<String, String> status = participantService.getParticipantStatus(eventId, authentication.getName());
        return ResponseEntity.ok(status);
    }

    /**
     * Retrieves the participation status for a specific user in an event. (Admin/Organizer only)
     * @param eventId The ID of the event.
     * @param userId The ID of the user whose status is to be checked.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with a map containing the status.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Map<String, String>> getParticipantStatusForUser(@PathVariable Long eventId, @PathVariable Long userId, Authentication authentication) {
        Map<String, String> status = participantService.getParticipantStatusForUser(eventId, userId, authentication.getName());
        return ResponseEntity.ok(status);
    }

    /**
     * Retrieves a paginated list of participants for an event.
     * @param eventId The ID of the event.
     * @param pageable Pagination and sorting information.
     * @return A ResponseEntity with a page of ParticipantDto objects.
     */
    @GetMapping
    public ResponseEntity<Page<ParticipantDto>> getParticipants(@PathVariable Long eventId, Pageable pageable) {
        Page<ParticipantDto> participants = participantService.getParticipantsForEvent(eventId, pageable);
        return ResponseEntity.ok(participants);
    }

    /**
     * Allows an authenticated user to leave an event.
     * @param eventId The ID of the event to leave.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveEvent(@PathVariable Long eventId, Authentication authentication) {
        participantService.leaveEvent(eventId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the status of a participant in an event. (Admin/Organizer only)
     * @param eventId The ID of the event.
     * @param userId The ID of the participant to update.
     * @param payload A map containing the new status.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the updated ParticipantDto.
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<ParticipantDto> updateParticipantStatus(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        String newStatus = payload.get("status");
        ParticipantDto updatedParticipant = participantService.updateParticipantStatus(eventId, userId, newStatus, authentication.getName());
        return ResponseEntity.ok(updatedParticipant);
    }
}