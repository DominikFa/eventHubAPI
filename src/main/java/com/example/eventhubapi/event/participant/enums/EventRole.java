package com.example.eventhubapi.event.participant.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the roles a user can have within an event (e.g., ORGANIZER, PARTICIPANT).
 */
public enum EventRole {
    ORGANIZER("organizer"),
    MODERATOR("moderator"),
    PARTICIPANT("participant");

    private final String value;

    EventRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static EventRole fromValue(String text) {
        for (EventRole b : EventRole.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}