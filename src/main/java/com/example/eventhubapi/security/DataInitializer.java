package com.example.eventhubapi.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * A component that runs on application startup to initialize necessary data,
 * such as user roles, if they don't already exist.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        // Check if roles already exist to avoid duplicates
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (roleRepository.findByName("ROLE_ORGANIZER").isEmpty()) {
            roleRepository.save(new Role("ROLE_ORGANIZER"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
    }
}