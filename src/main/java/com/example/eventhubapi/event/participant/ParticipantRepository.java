package com.example.eventhubapi.event.participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Participant.ParticipantId> {
    List<Participant> findByEventId(Long eventId);
    Optional<Participant> findByEventIdAndUserId(Long eventId, Long userId);
}