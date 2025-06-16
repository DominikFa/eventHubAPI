package com.example.eventhubapi.auth.dto;

import com.example.eventhubapi.user.dto.UserDto;

/**
 * DTO for sending back an authentication response, including the JWT and user details.
 */
public class AuthResponse {
    private String token;
    private UserDto user;

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}