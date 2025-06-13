package com.example.eventhubapi.event.participant;

import com.example.eventhubapi.event.Event;
import com.example.eventhubapi.event.participant.enums.EventRole;
import com.example.eventhubapi.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.eventhubapi.event.participant.enums.ParticipantStatus;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity representing the participation of a User in an Event.
 * This acts as a join table with extra information.
 */
@Entity
@Table(name = "participant")
@IdClass(Participant.ParticipantId.class)
@Getter
@Setter
@NoArgsConstructor
public class Participant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "status", nullable = false)
    private ParticipantStatus  status;


    @Column(name = "event_role", length = 30, nullable = false)
    private EventRole eventRole;

    public static class ParticipantId implements Serializable {
        private Long user;
        private Long event;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParticipantId that = (ParticipantId) o;
            return Objects.equals(user, that.user) && Objects.equals(event, that.event);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, event);
        }
    }
}