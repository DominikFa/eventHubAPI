package com.example.eventhubapi.user;

import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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

    @Transactional
    public void updateProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setAccount(user);
            user.setProfile(profile);
        }


        profile.setProfileImage(file.getBytes());

        userRepository.save(user); // Saving the user will cascade and save the profile
    }
}