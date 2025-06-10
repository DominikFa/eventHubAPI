package com.example.eventhubapi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO for capturing new user registration data.
 */
public class RegistrationRequest {

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Login cannot be empty")
    @Email(message = "Login should be a valid email")
    private String login;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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