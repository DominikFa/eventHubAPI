package com.example.eventhubapi.event;

import com.example.eventhubapi.common.dto.EventSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT new com.example.eventhubapi.common.dto.EventSummary(e.id, e.name, e.startDate, e.endDate) FROM Event e")
    Page<EventSummary> findAllSummary(Pageable pageable);

    // New query for public events
    @Query("SELECT e FROM Event e WHERE e.isPublic = true")
    Page<Event> findPublicEvents(Pageable pageable);

    // New query for events organized by a specific user
    @Query("SELECT e FROM Event e WHERE e.organizer.id = :organizerId")
    Page<Event> findByOrganizerId(@Param("organizerId") Long organizerId, Pageable pageable);

    // New query for events a user is participating in
    @Query("SELECT p.event FROM Participant p WHERE p.user.id = :userId")
    Page<Event> findEventsByParticipantId(@Param("userId") Long userId, Pageable pageable);
}