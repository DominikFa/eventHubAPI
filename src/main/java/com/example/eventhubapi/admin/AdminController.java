package com.example.eventhubapi.admin;

import com.example.eventhubapi.admin.dto.AdminChangeUserRoleRequest;
import com.example.eventhubapi.admin.dto.AdminEventUpdateRequest;
import com.example.eventhubapi.admin.dto.AdminUserUpdateStatusRequest;
import com.example.eventhubapi.event.dto.EventDto;
import com.example.eventhubapi.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // --- User Moderation Endpoints ---

    /**
     * Retrieves all users in the system.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Updates a user's status (e.g., ACTIVE, BANNED).
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long userId, @Valid @RequestBody AdminUserUpdateStatusRequest request) {
        UserDto updatedUser = adminService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Changes a user's role.
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDto> changeUserRole(@PathVariable Long userId, @Valid @RequestBody AdminChangeUserRoleRequest request) {
        UserDto updatedUser = adminService.changeUserRole(userId, request.getRoleName());
        return ResponseEntity.ok(updatedUser);
    }


    // --- Event Moderation Endpoints ---

    /**
     * Updates any event's details.
     */
    @PutMapping("/events/{eventId}")
    public ResponseEntity<EventDto> updateAnyEvent(@PathVariable Long eventId, @Valid @RequestBody AdminEventUpdateRequest request) {
        EventDto updatedEvent = adminService.updateAnyEvent(eventId, request);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Deletes any event from the system.
     */
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        adminService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }


    // --- Media Moderation Endpoints ---

    /**
     * Deletes any media file from the system.
     */
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long mediaId) {
        adminService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }
}