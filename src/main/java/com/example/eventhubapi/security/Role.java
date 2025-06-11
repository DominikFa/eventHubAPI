package com.example.eventhubapi.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_role")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_role_id")
    private Integer id;

    @Column(name = "role_name", length = 20, unique = true, nullable = false)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}