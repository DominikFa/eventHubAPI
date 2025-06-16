package com.example.eventhubapi.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user's profile, containing extended information like name,
 * description, and a profile image. It is linked one-to-one with the User entity.
 */
@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "account_id")
    private User account;

    @Column(length = 50)
    private String name;

    @Column(name = "profile_image", columnDefinition = "bytea")
    private byte[] profileImage;

    @Column(columnDefinition = "TEXT")
    private String description;
}