package com.example.eventhubapi.auth;

import com.example.eventhubapi.auth.dto.AuthResponse;
import com.example.eventhubapi.auth.dto.LoginRequest;
import com.example.eventhubapi.auth.dto.RegistrationRequest;
import com.example.eventhubapi.user.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling authentication and registration requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs an AuthController with the necessary services.
     * @param authService The service for authentication and registration logic.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for user login.
     * @param loginRequest DTO containing login credentials.
     * @return ResponseEntity containing the JWT and user details.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Endpoint for new user registration.
     * @param registrationRequest DTO containing details for the new user.
     * @return ResponseEntity containing the newly created user's details.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        UserDto registeredUser = authService.register(registrationRequest);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
}