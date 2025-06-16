package com.example.eventhubapi.security;

import com.example.eventhubapi.user.AccountStatus;
import com.example.eventhubapi.user.AccountStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes the database with default roles and account statuses on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountStatusRepository accountStatusRepository;

    /**
     * Constructs a DataInitializer with necessary repositories.
     * @param roleRepository The repository for Role entities.
     * @param accountStatusRepository The repository for AccountStatus entities.
     */
    public DataInitializer(RoleRepository roleRepository, AccountStatusRepository accountStatusRepository) {
        this.roleRepository = roleRepository;
        this.accountStatusRepository = accountStatusRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAccountStatuses();
    }

    private void initializeRoles() {
        if (roleRepository.findByName("user").isEmpty()) {
            roleRepository.save(new Role("user"));
        }
        if (roleRepository.findByName("organizer").isEmpty()) {
            roleRepository.save(new Role("organizer"));
        }
        if (roleRepository.findByName("admin").isEmpty()) {
            roleRepository.save(new Role("admin"));
        }
    }

    private void initializeAccountStatuses() {
        if (accountStatusRepository.findByStatusName("active").isEmpty()) {
            AccountStatus status = new AccountStatus();
            status.setStatusName("active");
            accountStatusRepository.save(status);
        }
        if (accountStatusRepository.findByStatusName("banned").isEmpty()) {
            AccountStatus status = new AccountStatus();
            status.setStatusName("banned");
            accountStatusRepository.save(status);
        }
        if (accountStatusRepository.findByStatusName("deactivated").isEmpty()) {
            AccountStatus status = new AccountStatus();
            status.setStatusName("deactivated");
            accountStatusRepository.save(status);
        }
    }
}