package com.example.eventhubapi.invitation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the possible statuses for an event invitation.
 */
public enum InvitationStatus {
    SENT("sent"),
    ACCEPTED("accepted"),
    DECLINED("declined"),
    REVOKED("revoked"),
    EXPIRED("expired");

    private final String value;

    InvitationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static InvitationStatus fromValue(String text) {
        for (InvitationStatus b : InvitationStatus.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}