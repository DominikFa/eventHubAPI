package com.example.eventhubapi.auth;

import com.example.eventhubapi.auth.dto.AuthResponse;
import com.example.eventhubapi.auth.dto.LoginRequest;
import com.example.eventhubapi.auth.dto.RegistrationRequest;
import com.example.eventhubapi.security.Role;
import com.example.eventhubapi.security.RoleRepository;
import com.example.eventhubapi.user.User;
import com.example.eventhubapi.user.UserRepository;
import com.example.eventhubapi.auth.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service class handling the business logic for authentication and registration.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user in the system.
     *
     * @param request DTO containing registration details.
     * @return The newly created User entity.
     */
    public User register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email is already in use.");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setCreatedAt(Instant.now());

        // Assign the default USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found."));
        newUser.setRole(userRole);

        // You can set a default status here if you have an AccountStatus entity/enum
        // newUser.setStatus(AccountStatus.ACTIVE);

        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user and returns a JWT.
     *
     * @param request DTO containing login credentials.
     * @return An AuthResponse containing the JWT and user details.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication."));

        String jwt = jwtService.generateToken(user);

        return new AuthResponse(jwt, user);
    }
}