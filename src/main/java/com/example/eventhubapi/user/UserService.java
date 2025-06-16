package com.example.eventhubapi.user;

import com.example.eventhubapi.common.dto.UserSummary;
import com.example.eventhubapi.user.dto.ChangePasswordRequest;
import com.example.eventhubapi.user.dto.UpdateProfileRequest;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.exception.UserNotFoundException;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;

/**
 * Service class for user-related business logic.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserService with necessary dependencies.
     * @param userRepository The repository for user data access.
     * @param userMapper The mapper for converting user entities to DTOs.
     * @param passwordEncoder The encoder for user passwords.
     */
    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves a user's full profile by their ID.
     * @param userId The ID of the user.
     * @return A UserDto containing the user's profile information.
     */
    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.toUserDto(user);
    }

    /**
     * Retrieves a user's summary by their ID.
     * @param userId The ID of the user.
     * @return A UserSummary DTO.
     */
    @Transactional(readOnly = true)
    public UserSummary getAccountSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return userMapper.toUserSummary(user);
    }

    /**
     * Retrieves a paginated and filterable list of all user summaries.
     * @param pageable Pagination and sorting information.
     * @param name Optional filter for user's profile name.
     * @param login Optional filter for user's login.
     * @param role Optional filter for user's role name.
     * @param status Optional filter for user's account status.
     * @return A Page of UserSummary objects.
     */
    @Transactional(readOnly = true)
    public Page<UserSummary> getAllUserSummaries(
            Pageable pageable,
            String name,
            String login,
            String role,
            String status) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("profile").get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (login != null && !login.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("login")), "%" + login.toLowerCase() + "%"));
            }
            if (role != null && !role.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("role").get("name")), role.toLowerCase()));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("status").get("statusName")), status.toLowerCase()));
            }

            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toUserSummary);
    }

    /**
     * Updates a user's profile information.
     * @param userId The ID of the user to update.
     * @param request The request DTO with the new profile data.
     * @return The updated UserDto.
     */
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

    /**
     * Changes a user's password.
     * @param userId The ID of the user.
     * @param request The request DTO with old and new passwords.
     */
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

    /**
     * Updates a user's profile image.
     * @param userId The ID of the user.
     * @param file The new profile image file.
     * @throws IOException If an I/O error occurs.
     */
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

    /**
     * Retrieves a user's profile image.
     * @param userId The ID of the user.
     * @return A byte array of the image data.
     */
    @Transactional(readOnly = true)
    public byte[] getProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (user.getProfile() == null || user.getProfile().getProfileImage() == null) {
            throw new UserNotFoundException("Profile image not found for user with id: " + userId);
        }
        return user.getProfile().getProfileImage();
    }

    /**
     * Deletes a user account.
     * @param userId The ID of the user to delete.
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}