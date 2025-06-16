package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.media.enums.MediaType;
import com.example.eventhubapi.event.media.enums.MediaUsage;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a media file associated with an event, such as a gallery image,
 * a schedule document, or a logo.
 */
@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long id;

    @Column(name = "media_file", nullable = false, columnDefinition = "bytea")
    private byte[] mediaFile;

    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private MediaUsage usage;

    @Column(name = "uploaded_at", updatable = false, nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private User uploader;
}