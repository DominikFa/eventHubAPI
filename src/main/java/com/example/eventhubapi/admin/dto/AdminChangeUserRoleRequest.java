package com.example.eventhubapi.admin.dto;

import jakarta.validation.constraints.NotEmpty;


/**
 * DTO for an admin to change a user's role.
 */

public class AdminChangeUserRoleRequest {

    @NotEmpty(message = "Role name cannot be empty")
    private String roleName;


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}