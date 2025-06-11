package com.example.eventhubapi.user.mapper;

import com.example.eventhubapi.user.Profile;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Mapper utility to convert between User entity and User DTOs.
 */
@Service
public class UserMapper {

    /**
     * Converts a User entity to a UserDto.
     *
     * @param user The User entity.
     * @return The corresponding UserDto.
     */
    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setRole(user.getRole().getName());
        dto.setStatus(user.getStatus().getStatusName());
        dto.setCreatedAt(user.getCreatedAt());

        Profile profile = user.getProfile();
        if (profile != null) {
            dto.setName(profile.getName());
            dto.setDescription(profile.getDescription());

            // If a profile image exists, generate the full, absolute URL for it.
            if (profile.getProfileImage() != null && profile.getProfileImage().length > 0) {
                String imageUrl = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/users/")
                        .path(String.valueOf(user.getId()))
                        .path("/profile-image")
                        .toUriString();
                dto.setProfileImageUrl(imageUrl);
            }
        }

        return dto;
    }
}