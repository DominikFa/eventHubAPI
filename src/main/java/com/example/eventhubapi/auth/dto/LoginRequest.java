package com.example.eventhubapi.auth.dto;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for capturing user login data.
 */
public class LoginRequest {

    @NotEmpty(message = "Login cannot be empty")
    private String login;

    @NotEmpty(message = "Password cannot be empty")
    private String password;

    // Getters and Setters
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}