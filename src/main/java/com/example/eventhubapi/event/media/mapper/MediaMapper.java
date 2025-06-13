package com.example.eventhubapi.event.media.mapper;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.event.media.Media;
import com.example.eventhubapi.event.media.dto.MediaDto;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Service
public class MediaMapper {
    private final UserMapper userMapper;

    public MediaMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public MediaDto toDto(Media media) {
        if (media == null) return null;

        MediaDto dto = new MediaDto();
        dto.setId(media.getId());
        dto.setMediaType(media.getMediaType().name());
        dto.setUsage(media.getUsage().name());
        dto.setUploadedAt(media.getUploadedAt());

        if (media.getUploader() != null) {
            dto.setUploader(userMapper.toUserSummary(media.getUploader()));
        }

        dto.setDownloadUrl(buildDownloadUrl(media));


        return dto;
    }

    private String buildDownloadUrl(Media media) {
        if (media.getId() == null) {
            return null;
        }

        String basePath = "/api/media/";
        String usagePath = media.getUsage().getValue(); // "gallery", "schedule", "logo"
        String id = media.getId().toString();

        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(basePath)
                .path(usagePath)
                .path("/")
                .path(id)
                .toUriString();
    }
}