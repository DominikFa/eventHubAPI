package com.example.eventhubapi.admin;

import com.example.eventhubapi.admin.dto.AdminChangeUserRoleRequest;
import com.example.eventhubapi.admin.dto.AdminEventUpdateRequest;
import com.example.eventhubapi.admin.dto.AdminUserUpdateStatusRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.event.media.MediaService;
import com.example.eventhubapi.event.media.dto.MediaDto;
import com.example.eventhubapi.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for administrative actions.
 * All endpoints in this controller require the user to have the 'ADMIN' role.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('admin')")
public class AdminController {

    private final AdminService adminService;
    private final MediaService mediaService;

    /**
     * Constructs an AdminController with the necessary services.
     * @param adminService The service for administrative actions.
     * @param mediaService The service for media-related actions.
     */
    public AdminController(AdminService adminService, MediaService mediaService) {
        this.adminService = adminService;
        this.mediaService = mediaService;
    }

    /**
     * Updates the status of a user's account.
     * @param id The ID of the user to update.
     * @param request The request body containing the new status.
     * @return A ResponseEntity with the updated UserDto.
     */
    @PatchMapping("/accounts/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateStatusRequest request) {
        UserDto updatedUser = adminService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Changes the role of a user.
     * @param id The ID of the user whose role is to be changed.
     * @param request The request body containing the new role name.
     * @return A ResponseEntity with the updated UserDto.
     */
    @PatchMapping("/accounts/{id}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id, @Valid @RequestBody AdminChangeUserRoleRequest request) {
        UserDto updatedUser = adminService.changeUserRole(id, request.getRoleName());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user account.
     * @param id The ID of the user to delete.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the details of any event.
     * @param id The ID of the event to update.
     * @param request The request body containing the updated event details.
     * @return A ResponseEntity with the updated EventDto.
     */
    @PutMapping("/events/{id}")
    public ResponseEntity<EventDto> updateAnyEvent(@PathVariable Long id, @Valid @RequestBody AdminEventUpdateRequest request) {
        EventDto updatedEvent = adminService.updateAnyEvent(id, request);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Deletes an event.
     * @param id The ID of the event to delete.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads a gallery image for an event as an administrator.
     * @param id The ID of the event.
     * @param file The image file to upload.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity containing the DTO of the created media.
     * @throws IOException If an I/O error occurs during file processing.
     */
    @PostMapping("/events/{id}/media/gallery")
    public ResponseEntity<MediaDto> adminUploadGalleryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.adminUploadGalleryImage(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }

    /**
     * Deletes a media file.
     * @param fileId The ID of the media file to delete.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/media/{fileId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long fileId) {
        adminService.deleteMedia(fileId);
        return ResponseEntity.noContent().build();
    }
}