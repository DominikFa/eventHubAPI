package com.example.eventhubapi.notification.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationStatus {
    CREATED("created"),
    READ("read"),
    DISMISSED("dismissed"),
    EXPIRED("expired");

    private final String value;

    NotificationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static NotificationStatus fromValue(String text) {
        for (NotificationStatus b : NotificationStatus.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}