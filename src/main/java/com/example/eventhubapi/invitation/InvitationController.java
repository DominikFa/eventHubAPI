package com.example.eventhubapi.invitation;

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

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<InvitationDto> createInvitation(@Valid @RequestBody InvitationCreateRequest request, Authentication authentication) {
        InvitationDto createdInvitation = invitationService.createInvitation(request, authentication.getName());
        return new ResponseEntity<>(createdInvitation, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<InvitationDto>> getMyInvitations(Authentication authentication, Pageable pageable) {
        Page<InvitationDto> invitations = invitationService.getInvitationsForUser(authentication.getName(), pageable);
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<InvitationDto> acceptInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto updatedInvitation = invitationService.acceptInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(updatedInvitation);
    }

    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<InvitationDto> declineInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto updatedInvitation = invitationService.declineInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(updatedInvitation);
    }


    @PostMapping("/{invitationId}/revoke")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<InvitationDto> revokeInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto revokedInvitation = invitationService.revokeInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(revokedInvitation);
    }
}