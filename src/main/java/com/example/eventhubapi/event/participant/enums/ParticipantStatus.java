package com.example.eventhubapi.event.participant.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ParticipantStatus {
    BANNED("banned"),
    ATTENDING("attending"),
    CANCELLED("cancelled");

    private final String value;

    ParticipantStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ParticipantStatus fromValue(String text) {
        for (ParticipantStatus b : ParticipantStatus.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}