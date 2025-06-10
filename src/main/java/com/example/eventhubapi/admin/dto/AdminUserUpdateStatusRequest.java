package com.example.eventhubapi.admin.dto;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for an admin to update a user's account status.
 */
public class AdminUserUpdateStatusRequest {

    @NotEmpty(message = "Status cannot be empty")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}