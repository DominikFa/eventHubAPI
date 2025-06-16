package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.media.enums.MediaUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Media entity.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByEventIdAndUsage(Long eventId, MediaUsage usage);
    Optional<Media> findOneByEventIdAndUsage(Long eventId, MediaUsage usage);
    List<Media> findByEventId(Long eventId);
}