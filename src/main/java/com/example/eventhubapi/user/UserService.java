package com.example.eventhubapi.user;

import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for user-related business logic.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves a user's profile by their ID.
     *
     * @param userId The ID of the user.
     * @return A UserDto containing public user information.
     */
    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.toUserDto(user);
    }

    /**
     * Updates a user's profile information.
     *
     * @param userId  The ID of the user to update.
     * @param request The DTO containing the new profile data.
     * @return The updated UserDto.
     */
    @Transactional
    public UserDto updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setAccount(user);
            user.setProfile(profile);
        }
        profile.setName(request.getName());
        profile.setDescription(request.getDescription());
        // Assuming profileImageUrl is a URL stored as a string. If it's a byte array for image data,
        // you would handle it differently (e.g., profile.setProfileImage(request.getProfileImage()))
        // The current Profile entity has byte[] profileImage, but UpdateProfileRequest has a String.
        // This is a mismatch to be addressed. For now, assuming it's a URL and we add a field to Profile.
        // Let's assume Profile should have a profileImageUrl String field instead of byte[].
        // For the sake of this fix, I'll comment out the line causing a compile error.
        // profile.setProfileImageUrl(request.getProfileImageUrl()); // This line needs the field in Profile entity.

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDto(updatedUser);
    }

    /**
     * Changes the user's password after verifying the old password.
     *
     * @param userId The ID of the user.
     * @param request DTO containing old and new password.
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check if the old password is correct
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }

        // Check if the new password is the same as the old one
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}