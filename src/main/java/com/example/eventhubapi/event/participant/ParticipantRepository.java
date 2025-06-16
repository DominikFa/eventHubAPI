package com.example.eventhubapi.event.participant;

import com.example.eventhubapi.event.participant.enums.ParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Participant entity.
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Participant.ParticipantId> {
    Page<Participant> findByEventId(Long eventId, Pageable pageable);

    Optional<Participant> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, ParticipantStatus status);
}