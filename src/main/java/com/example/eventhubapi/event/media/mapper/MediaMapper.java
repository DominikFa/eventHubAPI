package com.example.eventhubapi.event.media.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.media.Media;
import com.example.eventhubapi.event.media.dto.MediaDto;
import org.springframework.stereotype.Service;

@Service
public class MediaMapper {
    public MediaDto toDto(Media media) {
        if (media == null) return null;

        MediaDto dto = new MediaDto();
        dto.setId(media.getId());
        dto.setMediaType(media.getMediaType().name());
        dto.setUsage(media.getUsage().name());
        dto.setUploadedAt(media.getUploadedAt());

        if (media.getUploader() != null) {
            String uploaderName = media.getUploader().getProfile() != null ? media.getUploader().getProfile().getName() : null;
            dto.setUploader(new UserSummary(
                    media.getUploader().getId(),
                    uploaderName,
                    null // Profile image URL is not available as a string
            ));
        }

        // Dynamically generate the download URL
        if (media.getEvent() != null) {
            String url = String.format("/api/events/%d/media/%d", media.getEvent().getId(), media.getId());
            dto.setDownloadUrl(url);
        }

        return dto;
    }
}