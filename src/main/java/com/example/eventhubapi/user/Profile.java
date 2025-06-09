package com.example.eventhubapi.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Lob
    @Column(name = "profile_image")
    private byte[] profileImage;

    @Column(columnDefinition = "TEXT")
    private String description;
}