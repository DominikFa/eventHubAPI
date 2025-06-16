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

/**
 * REST controller for handling media uploads, downloads, and deletions.
 */
@RestController
@RequestMapping("/api")
public class MediaController {

    private final MediaService mediaService;

    /**
     * Constructs a MediaController with the necessary MediaService.
     * @param mediaService The service for media-related business logic.
     */
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Uploads a gallery image for a specific event.
     * @param id The ID of the event.
     * @param file The image file to upload.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the created MediaDto and HTTP status 201.
     * @throws IOException if an I/O error occurs.
     */
    @PostMapping("/events/{id}/media/gallery")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MediaDto> uploadGalleryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadGalleryImage(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    /**
     * Uploads a logo for a specific event.
     * @param id The ID of the event.
     * @param file The logo file to upload.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the created MediaDto and HTTP status 201.
     * @throws IOException if an I/O error occurs.
     */
    @PostMapping("/events/{id}/media/logo")
    @PreAuthorize("hasAuthority('organizer')")
    public ResponseEntity<MediaDto> uploadLogo(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadLogo(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    /**
     * Uploads a schedule for a specific event.
     * @param id The ID of the event.
     * @param file The schedule file to upload.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the created MediaDto and HTTP status 201.
     * @throws IOException if an I/O error occurs.
     */
    @PostMapping("/events/{id}/media/schedule")
    @PreAuthorize("hasAuthority('organizer')")
    public ResponseEntity<MediaDto> uploadSchedule(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.uploadSchedule(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    /**
     * Downloads a gallery media file.
     * @param fileId The ID of the file to download.
     * @return A ResponseEntity containing the media resource.
     */
    @GetMapping("/media/gallery/{fileId}")
    public ResponseEntity<Resource> downloadGalleryMedia(@PathVariable Long fileId) {
        Media media = mediaService.getMediaFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMediaType().getValue()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(media.getMediaFile()));
    }

    /**
     * Downloads a schedule file.
     * @param fileId The ID of the schedule file to download.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity containing the schedule resource.
     */
    @GetMapping("/media/schedule/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadScheduleMedia(@PathVariable Long fileId, Authentication authentication) {
        Media media = mediaService.getScheduleFile(fileId, authentication);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.getMediaType().getValue()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(media.getMediaFile()));
    }

    /**
     * Deletes a user's own gallery media file.
     * @param fileId The ID of the media file to delete.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/media/gallery/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOwnGalleryMedia(@PathVariable Long fileId, Authentication authentication) {
        mediaService.deleteOwnGalleryMedia(fileId, authentication);
        return ResponseEntity.noContent().build();
    }

    /**
     * Allows an organizer to delete a gallery media file from their event.
     * @param id The ID of the event.
     * @param fileId The ID of the media file to delete.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/events/{id}/media/gallery/{fileId}")
    @PreAuthorize("hasAuthority('organizer')")
    public ResponseEntity<Void> organizerDeleteGalleryMedia(@PathVariable Long id, @PathVariable Long fileId, Authentication authentication) {
        mediaService.organizerDeleteGalleryMedia(id, fileId, authentication);
        return ResponseEntity.noContent().build();
    }
}