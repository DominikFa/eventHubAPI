// File: eventHubAPI/src/main/java/com/example/eventhubapi/user/UserController.java

package com.example.eventhubapi.user;

import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.common.dto.UserSummary;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/accounts/{id}/summary")
    public ResponseEntity<UserSummary> getAccountSummary(@PathVariable Long id) {
        UserSummary summary = userService.getAccountSummary(id);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<UserDto> getAccount(@PathVariable Long id) {
        UserDto userDto = userService.getUserProfile(id);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Retrieves a paginated and filterable list of all user summaries.
     * Accessible by users with 'organizer' or 'admin' authority.
     * This endpoint is intended for scenarios like selecting users for invitations,
     * where only summary information is required.
     * @param pageable Pagination and sorting information.
     * @param name Optional filter for user's profile name (case-insensitive, contains).
     * @param login Optional filter for user's login (email) (case-insensitive, contains).
     * @param role Optional filter for user's role name (case-insensitive, exact match).
     * @param status Optional filter for user's account status (case-insensitive, exact match).
     * @return ResponseEntity with a page of UserSummary objects and HTTP status 200 OK.
     */
    @GetMapping("/accounts/summary/all")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Page<UserSummary>> getAllUserSummaries(
            Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        Page<UserSummary> userSummaries = userService.getAllUserSummaries(pageable, name, login, role, status); // Pass filters to service
        return ResponseEntity.ok(userSummaries);
    }

    @PutMapping("/account/profile")
    public ResponseEntity<UserDto> updateCurrentUser(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto updatedUser = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/account/password")
    public ResponseEntity<String> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/account/profile-image")
    public ResponseEntity<String> uploadProfileImage(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        userService.updateProfileImage(currentUser.getId(), file);
        return ResponseEntity.ok("Profile image updated successfully.");
    }

    @GetMapping("/users/{id}/profile-image")
    public ResponseEntity<Resource> getProfileImage(@PathVariable Long id) {
        byte[] imageBytes = userService.getProfileImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Assuming JPEG, could be dynamic
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(imageBytes));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}