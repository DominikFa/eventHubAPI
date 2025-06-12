package com.example.eventhubapi.event.media;

import com.example.eventhubapi.event.media.dto.MediaDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // --- Media Upload Endpoints ---

    @PostMapping("/events/{id}/media/gallery")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MediaDto> uploadGalleryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadGalleryImage(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    @PostMapping("/events/{id}/media/logo")
    @PreAuthorize("hasAuthority('organizer')")
    public ResponseEntity<MediaDto> uploadLogo(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadLogo(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    @PostMapping("/events/{id}/media/schedule")
    @PreAuthorize("hasAuthority('organizer')")
    public ResponseEntity<MediaDto> uploadSchedule(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadSchedule(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    // --- Media Download Endpoints ---

    @GetMapping("/media/gallery/{fileId}")
    public ResponseEntity<Resource> downloadGalleryMedia(@PathVariable Long fileId) {
        Media media = mediaService.getMediaFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMediaType().getValue()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(media.getMediaFile()));
    }

    @GetMapping("/media/schedule/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadScheduleMedia(@PathVariable Long fileId, Authentication authentication) {
        Media media = mediaService.getScheduleFile(fileId, authentication);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMediaType().getValue()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(media.getMediaFile()));
    }

    // --- Media Deletion Endpoints ---

    @DeleteMapping("/media/gallery/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOwnGalleryMedia(@PathVariable Long fileId, Authentication authentication) {
        mediaService.deleteOwnGalleryMedia(fileId, authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/events/{id}/media/gallery/{fileId}")
    @PreAuthorize("hasAuthority('organizer')")
    public ResponseEntity<Void> organizerDeleteGalleryMedia(@PathVariable Long id, @PathVariable Long fileId, Authentication authentication) {
        mediaService.organizerDeleteGalleryMedia(id, fileId, authentication);
        return ResponseEntity.noContent().build();
    }
}