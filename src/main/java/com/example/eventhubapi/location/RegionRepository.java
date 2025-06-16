package com.example.eventhubapi.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Region entity.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCodeAndCountry(String code, Country country);
}