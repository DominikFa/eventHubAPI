package com.example.eventhubapi.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}