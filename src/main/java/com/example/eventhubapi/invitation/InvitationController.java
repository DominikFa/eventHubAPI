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

/**
 * REST controller for managing event invitations.
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * Constructs an InvitationController with the necessary InvitationService.
     * @param invitationService The service for invitation-related business logic.
     */
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Creates and sends an invitation to a user for an event. (Admin/Organizer only)
     * @param request The request body containing invitation details.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the created InvitationDto and HTTP status 201.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<InvitationDto> createInvitation(@Valid @RequestBody InvitationCreateRequest request, Authentication authentication) {
        InvitationDto createdInvitation = invitationService.createInvitation(request, authentication.getName());
        return new ResponseEntity<>(createdInvitation, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of invitations received by the current user.
     * @param authentication The authentication object of the current user.
     * @param pageable Pagination and sorting information.
     * @return A ResponseEntity with a page of InvitationDto objects.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<InvitationDto>> getMyInvitations(Authentication authentication, Pageable pageable) {
        Page<InvitationDto> invitations = invitationService.getInvitationsForUser(authentication.getName(), pageable);
        return ResponseEntity.ok(invitations);
    }

    /**
     * Accepts an event invitation.
     * @param invitationId The ID of the invitation to accept.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the updated InvitationDto.
     */
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<InvitationDto> acceptInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto updatedInvitation = invitationService.acceptInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(updatedInvitation);
    }

    /**
     * Declines an event invitation.
     * @param invitationId The ID of the invitation to decline.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the updated InvitationDto.
     */
    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<InvitationDto> declineInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto updatedInvitation = invitationService.declineInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(updatedInvitation);
    }

    /**
     * Revokes a sent event invitation. (Admin/Organizer only)
     * @param invitationId The ID of the invitation to revoke.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the updated InvitationDto.
     */
    @PostMapping("/{invitationId}/revoke")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<InvitationDto> revokeInvitation(@PathVariable Long invitationId, Authentication authentication) {
        InvitationDto revokedInvitation = invitationService.revokeInvitation(invitationId, authentication.getName());
        return ResponseEntity.ok(revokedInvitation);
    }
}