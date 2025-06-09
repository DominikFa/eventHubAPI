package com.example.eventhubapi.event.participant;

import com.example.eventhubapi.event.participant.dto.ParticipantDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<ParticipantDto>> getParticipants(@PathVariable Long eventId) {
        List<ParticipantDto> participants = participantService.getParticipantsForEvent(eventId);
        return ResponseEntity.ok(participants);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveEvent(@PathVariable Long eventId, Authentication authentication) {
        participantService.leaveEvent(eventId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}