package com.example.eventhubapi.auth;

import com.example.eventhubapi.auth.dto.AuthResponse;
import com.example.eventhubapi.auth.dto.LoginRequest;
import com.example.eventhubapi.auth.dto.RegistrationRequest;
import com.example.eventhubapi.security.Role;
import com.example.eventhubapi.security.RoleRepository;
import com.example.eventhubapi.user.*;
import com.example.eventhubapi.user.dto.UserDto;
import com.example.eventhubapi.user.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserMapper userMapper;
    private final AccountStatusRepository accountStatusRepository;

    /**
     * Constructs an AuthService with the necessary components.
     * @param userRepository The repository for user data access.
     * @param roleRepository The repository for role data access.
     * @param passwordEncoder The encoder for user passwords.
     * @param jwtService The service for JWT operations.
     * @param authenticationManager The manager for handling authentication.
     * @param userMapper The mapper for converting user entities to DTOs.
     * @param accountStatusRepository The repository for account status data access.
     */
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UserMapper userMapper,
                       AccountStatusRepository accountStatusRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.accountStatusRepository = accountStatusRepository;
    }

    /**
     * Registers a new user in the system.
     * @param request DTO containing registration details.
     * @return The DTO of the newly created user.
     */
    @Transactional
    public UserDto register(RegistrationRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new IllegalStateException("Login is already in use.");
        }

        User newUser = new User();
        newUser.setLogin(request.getLogin());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setCreatedAt(Instant.now());

        Role userRole = roleRepository.findByName("user")
                .orElseThrow(() -> new IllegalStateException("Default role not found."));
        newUser.setRole(userRole);

        AccountStatus activeStatus = accountStatusRepository.findByStatusName("active")
                .orElseThrow(() -> new IllegalStateException("Default account status 'active' not found."));
        newUser.setStatus(activeStatus);

        Profile profile = new Profile();
        profile.setName(request.getName());
        profile.setAccount(newUser);
        newUser.setProfile(profile);


        User savedUser = userRepository.save(newUser);
        return userMapper.toUserDto(savedUser);
    }

    /**
     * Authenticates a user and returns a JWT.
     * @param request DTO containing login credentials.
     * @return An AuthResponse containing the JWT and user details.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication."));

        String jwt = jwtService.generateToken(user);

        return new AuthResponse(jwt, userMapper.toUserDto(user));
    }
}