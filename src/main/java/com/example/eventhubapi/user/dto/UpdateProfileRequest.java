package com.example.eventhubapi.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating a user's profile information.
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    private String description;
}