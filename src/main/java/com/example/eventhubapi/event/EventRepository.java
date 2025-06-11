package com.example.eventhubapi.event;

import com.example.eventhubapi.common.dto.EventSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT new com.example.eventhubapi.common.dto.EventSummary(e.id, e.name, e.startDate, e.endDate) FROM Event e")
    Page<EventSummary> findAllSummary(Pageable pageable);
}