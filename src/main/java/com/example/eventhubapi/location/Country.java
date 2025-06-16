package com.example.eventhubapi.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a country, identified by its name and a unique ISO code.
 */
@Entity
@Table(name = "country")
@Getter
@Setter
@NoArgsConstructor
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "iso_code", length = 3, nullable = false, unique = true)
    private String isoCode;
}