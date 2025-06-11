package com.example.eventhubapi.user;

import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * REST controller for user-related operations, such as retrieving and updating profiles.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param authentication The authentication object provided by Spring Security.
     * @return A DTO containing the current user's public profile information.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(userDto);
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param authentication The authentication object.
     * @param request        The DTO containing the fields to update.
     * @return The updated user profile DTO.
     */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto updatedUser = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Changes the password for the currently authenticated user.
     *
     * @param authentication The authentication object.
     * @param request The DTO containing the old and new passwords.
     * @return A success message.
     */
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = (User) authentication.getPrincipal();
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PutMapping("/me/profile-image")
    public ResponseEntity<String> uploadProfileImage(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        userService.updateProfileImage(currentUser.getId(), file);
        return ResponseEntity.ok("Profile image updated successfully.");
    }
}