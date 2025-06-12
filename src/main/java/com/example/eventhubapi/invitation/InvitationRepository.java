package com.example.eventhubapi.invitation;

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
}