package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.media.enums.MediaUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * Finds a collection of media for an event with a specific usage.
     * Used for fetching all gallery images or all schedule documents.
     */
    List<Media> findByEventIdAndUsage(Long eventId, MediaUsage usage);

    /**
     * Finds a single media item for an event with a specific usage.
     * Used to find a unique logo or schedule for an event.
     */
    Optional<Media> findOneByEventIdAndUsage(Long eventId, MediaUsage usage);

    /**
     * Finds all media for a given event, regardless of usage.
     */
    List<Media> findByEventId(Long eventId);
}