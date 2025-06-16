package com.example.eventhubapi.invitation;

import com.example.eventhubapi.invitation.enums.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Invitation entity.
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    /**
     * Finds all invitations sent to a specific user.
     * @param userId The ID of the invited user.
     * @return A list of invitations.
     */
    Page<Invitation> findByInvitedUserId(Long userId, Pageable pageable);

    /**
     * Checks if an invitation with a specific status exists for a given event and user.
     * @param eventId The ID of the event.
     * @param invitedUserId The ID of the invited user.
     * @param status The status of the invitation to check for.
     * @return true if such an invitation exists, false otherwise.
     */
    boolean existsByEventIdAndInvitedUserIdAndStatus(Long eventId, Long invitedUserId, InvitationStatus status);
}