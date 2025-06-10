package com.example.eventhubapi.user.mapper;

import com.example.eventhubapi.user.Profile;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.dto.UserDto;
import org.springframework.stereotype.Service;

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
            // The profileImageUrl is not in the Profile entity, so this would be null.
            // dto.setProfileImageUrl(profile.getProfileImageUrl());
        }

        return dto;
    }
}