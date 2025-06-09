package com.example.eventhubapi.invitation;

import com.example.eventhubapi.invitation.dto.InvitationCreateRequest;
import com.example.eventhubapi.invitation.dto.InvitationDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing invitations.
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Creates and sends an invitation.
     * @param request The request DTO containing event and invited user IDs.
     * @param authentication The current user sending the invitation.
     * @return The created invitation DTO.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<InvitationDto> createInvitation(@Valid @RequestBody InvitationCreateRequest request, Authentication authentication) {
        InvitationDto createdInvitation = invitationService.createInvitation(request, authentication.getName());
        return new ResponseEntity<>(createdInvitation, HttpStatus.CREATED);
    }

    /**
     * Retrieves all invitations for the currently authenticated user.
     * @param authentication The current user.
     * @return A list of invitation DTOs.
     */
    @GetMapping("/my-invitations")
    public ResponseEntity<List<InvitationDto>> getMyInvitations(Authentication authentication) {
        List<InvitationDto> invitations = invitationService.getInvitationsForUser(authentication.getName());
        return ResponseEntity.ok(invitations);
    }

    /**
     * Accepts an invitation.
     * @param invitationId The ID of the invitation to accept.
     * @param authentication The user accepting the invitation.
     * @return The updated invitation DTO.
     */
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<InvitationDto> acceptInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto updatedInvitation = invitationService.acceptInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(updatedInvitation);
    }

    /**
     * Declines an invitation.
     * @param invitationId The ID of the invitation to decline.
     * @param authentication The user declining the invitation.
     * @return The updated invitation DTO.
     */
    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<InvitationDto> declineInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto updatedInvitation = invitationService.declineInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(updatedInvitation);
    }
}