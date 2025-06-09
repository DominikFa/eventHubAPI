package com.example.eventhubapi.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for changing a user's password.
 */
@Getter
@Setter
public class ChangePasswordRequest {
    @NotEmpty(message = "Old password cannot be empty")
    private String oldPassword;

    @NotEmpty(message = "New password cannot be empty")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;
}