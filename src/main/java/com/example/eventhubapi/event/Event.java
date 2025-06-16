package com.example.eventhubapi.event;

import com.example.eventhubapi.event.participant.Participant;
import com.example.eventhubapi.location.Location;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * The core entity representing an event. It holds details such as name,
 * description, dates, location, and its participants.
 */
@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "max_participants")
    private Long maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User organizer;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;

    @OneToMany(mappedBy = "event")
    private Set<Participant> participants = new HashSet<>();
}