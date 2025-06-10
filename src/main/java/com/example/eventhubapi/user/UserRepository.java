package com.example.eventhubapi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their login.
     *
     * @param login The login to search for.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByLogin(String login);

    /**
     * Checks if a user exists with the given login.
     *
     * @param login The login to check.
     * @return True if a user with the login exists, false otherwise.
     */
    boolean existsByLogin(String login);
}