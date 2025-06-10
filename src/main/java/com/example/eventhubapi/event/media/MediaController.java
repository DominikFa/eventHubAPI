package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.media.dto.MediaDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Uploads media for a specific event.
     *
     * @param eventId The ID of the event.
     * @param file The media file to upload.
     * @param usage The intended usage of the media (e.g., GALLERY, LOGO).
     * @param authentication The current user's authentication details.
     * @return DTO of the uploaded media.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<MediaDto> uploadMedia(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("usage") String usage,
            Authentication authentication) throws IOException {

        MediaDto mediaDto = mediaService.store(file, eventId, usage, authentication.getName());
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    /**
     * Retrieves metadata for all media associated with a specific event.
     *
     * @param eventId The ID of the event.
     * @return A list of media DTOs (without the file data).
     */
    @GetMapping
    public ResponseEntity<List<MediaDto>> getMediaForEvent(@PathVariable Long eventId) {
        List<MediaDto> mediaList = mediaService.getMediaForEvent(eventId);
        return ResponseEntity.ok(mediaList);
    }

    /**
     * Downloads a specific media file by its ID.
     *
     * @param eventId The ID of the event (used for URL consistency).
     * @param mediaId The ID of the media file to download.
     * @return The media file as a downloadable resource.
     */
    @GetMapping("/{mediaId}")
    public ResponseEntity<Resource> downloadMedia(@PathVariable Long eventId, @PathVariable Long mediaId) {
        Media media = mediaService.getMediaFile(mediaId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"media_" + media.getId() + "\"")
                .contentType(org.springframework.http.MediaType.valueOf(media.getMediaType().name().replace("_", "/")))
                .body(new ByteArrayResource(media.getMediaFile()));
    }
}