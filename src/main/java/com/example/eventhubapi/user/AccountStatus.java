package com.example.eventhubapi.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the status of a user's account (e.g., active, banned, deactivated).
 */
@Entity
@Table(name = "account_status")
@Getter
@Setter
@NoArgsConstructor
public class AccountStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_status_id")
    private Integer id;

    @Column(name = "status_name", length = 30, nullable = false)
    private String statusName;
}