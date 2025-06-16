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

/**
 * REST controller for user-related actions, such as profile management.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a UserController with the necessary UserService.
     * @param userService The service for user-related business logic.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with the UserDto of the current user.
     */
    @GetMapping("/account/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(userDto);
    }

    /**
     * Retrieves a summary of a user's account by ID.
     * @param id The ID of the user.
     * @return A ResponseEntity with the UserSummary.
     */
    @GetMapping("/accounts/{id}/summary")
    public ResponseEntity<UserSummary> getAccountSummary(@PathVariable Long id) {
        UserSummary summary = userService.getAccountSummary(id);
        return ResponseEntity.ok(summary);
    }

    /**
     * Retrieves the full details of a user's account by ID.
     * @param id The ID of the user.
     * @return A ResponseEntity with the UserDto.
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<UserDto> getAccount(@PathVariable Long id) {
        UserDto userDto = userService.getUserProfile(id);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Retrieves a paginated and filterable list of all user summaries.
     * Accessible by users with 'organizer' or 'admin' authority.
     * @param pageable Pagination and sorting information.
     * @param name Optional filter for user's profile name.
     * @param login Optional filter for user's login.
     * @param role Optional filter for user's role name.
     * @param status Optional filter for user's account status.
     * @return A ResponseEntity with a page of UserSummary objects.
     */
    @GetMapping("/accounts/summary/all")
    @PreAuthorize("hasAnyAuthority('organizer', 'admin')")
    public ResponseEntity<Page<UserSummary>> getAllUserSummaries(
            Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        Page<UserSummary> userSummaries = userService.getAllUserSummaries(pageable, name, login, role, status);
        return ResponseEntity.ok(userSummaries);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * @param authentication The authentication object of the current user.
     * @param request The request body containing the updated profile information.
     * @return A ResponseEntity with the updated UserDto.
     */
    @PutMapping("/account/profile")
    public ResponseEntity<UserDto> updateCurrentUser(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto updatedUser = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Changes the password of the currently authenticated user.
     * @param authentication The authentication object of the current user.
     * @param request The request body containing the old and new passwords.
     * @return A ResponseEntity with a success message.
     */
    @PutMapping("/account/password")
    public ResponseEntity<String> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    /**
     * Uploads or updates the profile image for the currently authenticated user.
     * @param authentication The authentication object of the current user.
     * @param file The image file to upload.
     * @return A ResponseEntity with a success message.
     * @throws IOException if an I/O error occurs.
     */
    @PostMapping("/account/profile-image")
    public ResponseEntity<String> uploadProfileImage(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        userService.updateProfileImage(currentUser.getId(), file);
        return ResponseEntity.ok("Profile image updated successfully.");
    }

    /**
     * Retrieves the profile image of a user by their ID.
     * @param id The ID of the user whose image is to be retrieved.
     * @return A ResponseEntity containing the image resource.
     */
    @GetMapping("/users/{id}/profile-image")
    public ResponseEntity<Resource> getProfileImage(@PathVariable Long id) {
        byte[] imageBytes = userService.getProfileImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new ByteArrayResource(imageBytes));
    }

    /**
     * Deletes the account of the currently authenticated user.
     * @param authentication The authentication object of the current user.
     * @return A ResponseEntity with no content.
     */
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}