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
@Table(name = "account")
@SecondaryTable(name = "account_auth", pkJoinColumns = @PrimaryKeyJoinColumn(name = "account_id"))
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(name = "login", table = "account_auth", nullable = false, unique = true)
    private String login;

    @Column(name = "password", table = "account_auth", nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    // MODIFIED: Added cascade = CascadeType.PERSIST and changed FetchType to LAZY
    @OneToOne(mappedBy = "account", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Profile profile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private AccountStatus status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getName()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        String statusName = status.getStatusName().toUpperCase();
        return !statusName.equals("BANNED") && !statusName.equals("DEACTIVATED");
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
