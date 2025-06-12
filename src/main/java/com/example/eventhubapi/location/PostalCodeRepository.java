package com.example.eventhubapi.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostalCodeRepository extends JpaRepository<PostalCode, Long> {
    Optional<PostalCode> findByCode(String code);
}