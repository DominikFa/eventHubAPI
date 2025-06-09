package com.example.eventhubapi.invitation;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.invitation.enums.InvitationStatus;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity representing an invitation for a user to join an event.
 */
@Entity
@Table(name = "invitation")
@Getter
@Setter
@NoArgsConstructor
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = false)
    private User invitedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviting_user_id", nullable = false)
    private User invitingUser;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InvitationStatus status;

    @Column(name = "sent_at", updatable = false, nullable = false)
    private Instant sentAt;

    @Column(name = "responded_at")
    private Instant respondedAt;
}