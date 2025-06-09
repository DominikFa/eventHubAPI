package com.example.eventhubapi.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// We need repositories for all location-related entities
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {}

@Repository
interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {
    // Custom query methods can be added here
}

@Repository
interface CityRepository extends JpaRepository<City, Long> {}

@Repository
interface RegionRepository extends JpaRepository<Region, Long> {}

@Repository
interface CountryRepository extends JpaRepository<Country, Long> {}