package com.example.eventhubapi.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for exposing public user data. This is the safe "public view" of a User.
 */
@Getter
@Setter
public class UserDto {
    private Long id;
    private String login;
    private String name;
    private String role;
    private String status;
    private String profileImageUrl;
    private String description;
    private Instant createdAt;
}