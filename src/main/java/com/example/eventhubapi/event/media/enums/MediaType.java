package com.example.eventhubapi.event.media.enums;

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

    private final String type;

    MediaType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}