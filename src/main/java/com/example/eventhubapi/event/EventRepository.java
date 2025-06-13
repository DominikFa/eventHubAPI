// File: eventHubAPI/src/main/java/com/example/eventhubapi/event/EventRepository.java
package com.example.eventhubapi.event;

import com.example.eventhubapi.common.dto.EventSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
// MODIFIED: Added JpaSpecificationExecutor for dynamic queries
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT new com.example.eventhubapi.common.dto.EventSummary(e.id, e.name, e.startDate, e.endDate) FROM Event e")
    Page<EventSummary> findAllSummary(Pageable pageable);


    @Query("SELECT e FROM Event e WHERE e.organizer.id = :organizerId")
    Page<Event> findByOrganizerId(@Param("organizerId") Long organizerId, Pageable pageable);

    @Query("SELECT p.event FROM Participant p WHERE p.user.id = :userId")
    Page<Event> findEventsByParticipantId(@Param("userId") Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findByIdWithPessimisticLock(@Param("eventId") Long eventId);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.participants WHERE e.id = :eventId")
    Optional<Event> findByIdWithParticipants(@Param("eventId") Long eventId);
}
