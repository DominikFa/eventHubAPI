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

@RestController
@RequestMapping("/api/events/{eventId}/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping
    public ResponseEntity<ParticipantDto> joinEvent(@PathVariable Long eventId, Authentication authentication) {
        ParticipantDto participant = participantService.joinEvent(eventId, authentication.getName());
        return new ResponseEntity<>(participant, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getParticipantStatus(@PathVariable Long eventId, Authentication authentication) {
        Map<String, String> status = participantService.getParticipantStatus(eventId, authentication.getName());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Map<String, String>> getParticipantStatusForUser(@PathVariable Long eventId, @PathVariable Long userId, Authentication authentication) {
        Map<String, String> status = participantService.getParticipantStatusForUser(eventId, userId, authentication.getName());
        return ResponseEntity.ok(status);
    }

    @GetMapping
    public ResponseEntity<Page<ParticipantDto>> getParticipants(@PathVariable Long eventId, Pageable pageable) {
        Page<ParticipantDto> participants = participantService.getParticipantsForEvent(eventId, pageable);
        return ResponseEntity.ok(participants);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveEvent(@PathVariable Long eventId, Authentication authentication) {
        participantService.leaveEvent(eventId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

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