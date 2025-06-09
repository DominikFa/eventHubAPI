package com.example.eventhubapi.user;

import com.example.eventhubapi.security.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "account") // Correct table name
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails { // You can keep implementing UserDetails for Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String login; // Changed from email to login

    // Password is now in a separate table/entity

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    // The relationship to the Profile table
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    // The relationship to the Role table
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_role_id", nullable = false)
    private Role role;

    // The relationship to the AccountStatus table
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_status_id", nullable = false)
    private AccountStatus status;


    // --- UserDetails Implementation ---
    // Note: We need to resolve where the password comes from for this to fully work.
    // For now, this is a placeholder. You'll likely need a custom query in your
    // UserDetailsServiceImpl to fetch the password from the 'account_auth' table.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getName()));
    }

    // This is a placeholder. Password logic needs to be handled in the service layer.
    @Override
    public String getPassword() {
        // This needs to be fetched from the 'account_auth' table.
        // Returning null here as it's not directly on this entity.
        return null;
    }

    @Override
    public String getUsername() {
        return this.login; // Use login for username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !status.getStatusName().equalsIgnoreCase("BANNED");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status.getStatusName().equalsIgnoreCase("ACTIVE");
    }
}