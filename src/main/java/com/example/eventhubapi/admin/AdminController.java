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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

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


    public AdminController(AdminService adminService, MediaService mediaService) {
        this.adminService = adminService;
        this.mediaService = mediaService;
    }

    // --- User Moderation Endpoints ---


    @PatchMapping("/accounts/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateStatusRequest request) {
        UserDto updatedUser = adminService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/accounts/{id}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long id, @Valid @RequestBody AdminChangeUserRoleRequest request) {
        UserDto updatedUser = adminService.changeUserRole(id, request.getRoleName());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    // --- Event Moderation Endpoints ---

    @PutMapping("/events/{id}")
    public ResponseEntity<EventDto> updateAnyEvent(@PathVariable Long id, @Valid @RequestBody AdminEventUpdateRequest request) {
        EventDto updatedEvent = adminService.updateAnyEvent(id, request);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // --- Media Moderation Endpoints ---

    @PostMapping("/events/{id}/media/gallery")
    public ResponseEntity<MediaDto> adminUploadGalleryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        MediaDto mediaDto = mediaService.adminUploadGalleryImage(id, file, authentication);
        return new ResponseEntity<>(mediaDto, HttpStatus.CREATED);
    }


    @DeleteMapping("/media/{fileId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long fileId) {
        adminService.deleteMedia(fileId);
        return ResponseEntity.noContent().build();
    }
}