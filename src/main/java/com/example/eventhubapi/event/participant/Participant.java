package com.example.eventhubapi.event.participant;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.participant.enums.EventRole;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing the participation of a User in an Event.
 * This acts as a join table with extra information.
 */
@Entity
@Table(name = "participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_role", length = 20, nullable = false)
    private EventRole eventRole;
}