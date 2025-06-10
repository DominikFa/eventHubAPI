package com.example.eventhubapi.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "city")
@Getter
@Setter
@NoArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "city_postal_code",
            joinColumns = @JoinColumn(name = "city_id"),
            inverseJoinColumns = @JoinColumn(name = "postal_code_id"))
    private Set<PostalCode> postalCodes;
}