package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Entity
@Table(name = "users")
@Data
@ToString(exclude = "passwordHash")
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //partial unique constraint in the V1.0.0 migration
    //partial nullable constraint in the V1.0.1 migration
    @Nullable
    private String username;

    //partial unique constraint in the V1.0.0 migration
    @Column(nullable = false)
    private String email;

    //partial nullable constraint in the V1.0.1 migration
    @Nullable
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Nullable
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    //partial nullable constraint in the V1.0.1 migration
    @Nullable
    private String providerId;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Nullable
    private Instant deletedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    @Nullable
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return deletedAt == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return deletedAt == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }
}
