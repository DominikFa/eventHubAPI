package com.example.eventhubapi.event.media.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the supported media types (MIME types) for file uploads.
 */
public enum MediaType {
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_GIF("image/gif"),
    IMAGE_WEBP("image/webp"),
    IMAGE_SVG_XML("image/svg+xml"),
    VIDEO_MP4("video/mp4"),
    VIDEO_WEBM("video/webm"),
    VIDEO_OGG("video/ogg"),
    APPLICATION_PDF("application/pdf");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static MediaType fromValue(String text) {
        for (MediaType b : MediaType.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}