package com.example.eventhubapi.user.mapper;

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
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole().getName());
        dto.setStatus(user.getStatus().name()); // Map the enum to its string name
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setDescription(user.getDescription());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }
}