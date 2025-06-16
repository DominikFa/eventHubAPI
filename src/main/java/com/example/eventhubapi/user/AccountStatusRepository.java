package com.example.eventhubapi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the AccountStatus entity.
 */
@Repository
public interface AccountStatusRepository extends JpaRepository<AccountStatus, Integer> {
    Optional<AccountStatus> findByStatusName(String statusName);
}