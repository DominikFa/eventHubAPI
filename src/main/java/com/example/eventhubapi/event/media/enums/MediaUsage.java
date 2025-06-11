package com.example.eventhubapi.event.media.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MediaUsage {
    GALLERY("gallery"),
    SCHEDULE("schedule"),
    LOGO("logo");

    private final String value;

    MediaUsage(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static MediaUsage fromValue(String text) {
        for (MediaUsage b : MediaUsage.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}