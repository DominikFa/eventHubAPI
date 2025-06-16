package com.example.eventhubapi.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the City entity.
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameAndRegion(String name, Region region);
}