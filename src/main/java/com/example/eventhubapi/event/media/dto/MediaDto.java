package com.example.eventhubapi.event.media.dto;

import com.example.eventhubapi.common.dto.UserSummary;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for exposing media file metadata to the client.
 */
@Getter
@Setter
public class MediaDto {
    private Long id;
    private String mediaType;
    private String usage;
    private Instant uploadedAt;
    private UserSummary uploader;
    private String downloadUrl;
}