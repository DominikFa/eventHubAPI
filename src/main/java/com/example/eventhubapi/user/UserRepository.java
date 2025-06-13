// File: eventHubAPI/src/main/java/com/example/eventhubapi/user/UserRepository.java
package com.example.eventhubapi.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
// MODIFIED: Added JpaSpecificationExecutor for dynamic queries
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdWithLock(@Param("userId") Long userId);

}
