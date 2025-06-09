package com.example.eventhubapi.auth.dto;

import com.example.eventhubapi.user.User;

/**
 * DTO for sending back an authentication response, including the JWT and user details.
 */
public class AuthResponse {
    private String token;
    private User user; // In a real app, this should be a UserDTO

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}