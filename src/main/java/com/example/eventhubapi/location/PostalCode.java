package com.example.eventhubapi.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

/**
 * Represents a postal code, which can be associated with multiple cities.
 */
@Entity
@Table(name = "postal_code")
@Getter
@Setter
@NoArgsConstructor
public class PostalCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postal_code_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToMany(mappedBy = "postalCodes")
    private Set<City> cities;
}