package com.example.eventhubapi.user;

import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.common.dto.UserSummary;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}