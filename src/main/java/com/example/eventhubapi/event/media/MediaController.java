package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.media.dto.MediaDto;
import com.example.eventhubapi.event.media.enums.MediaUsage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/events/{eventId}")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // --- GALLERY ENDPOINTS ---
    @PostMapping("/gallery")
    public ResponseEntity<MediaDto> uploadGalleryImage(@PathVariable Long eventId, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.addGalleryImage(eventId, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/gallery/{mediaId}")
    public ResponseEntity<Void> deleteGalleryImage(@PathVariable Long eventId, @PathVariable Long mediaId, Authentication authentication) {
        mediaService.deleteGalleryImage(eventId, mediaId, authentication);
        return ResponseEntity.noContent().build();
    }

    // --- LOGO ENDPOINTS ---
    @PostMapping("/logo")
    public ResponseEntity<MediaDto> uploadEventLogo(@PathVariable Long eventId, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadEventLogo(eventId, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/logo")
    public ResponseEntity<Void> deleteEventLogo(@PathVariable Long eventId, Authentication authentication) {
        mediaService.deleteRestrictedMedia(eventId, MediaUsage.LOGO, authentication);
        return ResponseEntity.noContent().build();
    }

    // --- SCHEDULE ENDPOINTS ---
    @PostMapping("/schedule")
    public ResponseEntity<MediaDto> uploadEventSchedule(@PathVariable Long eventId, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadEventSchedule(eventId, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/schedule")
    public ResponseEntity<Void> deleteEventSchedule(@PathVariable Long eventId, Authentication authentication) {
        mediaService.deleteRestrictedMedia(eventId, MediaUsage.SCHEDULE, authentication);
        return ResponseEntity.noContent().build();
    }

    // --- GENERIC MEDIA DOWNLOAD ---
    @GetMapping("/media/{mediaId}")
    public ResponseEntity<Resource> downloadMedia(@PathVariable Long eventId, @PathVariable Long mediaId) {
        Media media = mediaService.getMediaFile(mediaId);

        if (!media.getEvent().getId().equals(eventId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMediaType().getValue()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(media.getMediaFile()));
    }
}