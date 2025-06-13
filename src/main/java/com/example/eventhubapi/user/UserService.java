// File: eventHubAPI/src/main/java/com/example/eventhubapi/user/UserService.java

package com.example.eventhubapi.user;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Import Specification
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List
import jakarta.persistence.criteria.Predicate; // Import Predicate
import jakarta.persistence.criteria.Join; // Import Join

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

    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public UserSummary getAccountSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.toUserSummary(user);
    }

    /**
     * Retrieves a paginated list of all users as UserSummary objects.
     * This is useful for selection lists where full user details are not needed.
     * @param pageable Pagination information.
     * @param name Optional filter for user's profile name (case-insensitive, contains).
     * @param login Optional filter for user's login (email) (case-insensitive, contains).
     * @param role Optional filter for user's role name (case-insensitive, exact match).
     * @param status Optional filter for user's account status (case-insensitive, exact match).
     * @return A Page of UserSummary objects.
     */
    @Transactional(readOnly = true)
    public Page<UserSummary> getAllUserSummaries(
            Pageable pageable,
            String name,
            String login,
            String role,
            String status) {
        // MODIFIED: Moved filtering logic from repository to service using Specification
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                // Join to profile table for filtering by profile name
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("profile").get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (login != null && !login.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("login")), "%" + login.toLowerCase() + "%"));
            }
            if (role != null && !role.isEmpty()) {
                // Join to role table for filtering by role name
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("role").get("name")), role.toLowerCase()));
            }
            if (status != null && !status.isEmpty()) {
                // Join to status table for filtering by status name
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("status").get("statusName")), status.toLowerCase()));
            }

            // Ensure distinct results if multiple joins result in duplicates
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Use findAll with Specification
        return userRepository.findAll(spec, pageable)
                .map(userMapper::toUserSummary);
    }

    @Transactional
    public UserDto updateUserProfile(Long userId, UpdateProfileRequest request) {

        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setAccount(user);
            user.setProfile(profile);
        }

        if (request.getName() != null) {
            profile.setName(request.getName());
        }
        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDto(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }

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
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public byte[] getProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (user.getProfile() == null || user.getProfile().getProfileImage() == null) {
            throw new UserNotFoundException("Profile image not found for user with id: " + userId);
        }
        return user.getProfile().getProfileImage();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
