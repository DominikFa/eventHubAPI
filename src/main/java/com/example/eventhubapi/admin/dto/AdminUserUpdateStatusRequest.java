package com.example.eventhubapi.admin.dto;

import com.example.eventhubapi.user.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;


/**
 * DTO for an admin to update a user's account status.
 */

public class AdminUserUpdateStatusRequest {

    @NotNull(message = "Status cannot be null")
    private AccountStatus status;


    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}