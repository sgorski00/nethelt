package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileOperationNotAllowedException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;

@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"passwordHash", "identities"})
@EqualsAndHashCode(exclude = "identities")
@NoArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String email;

  // partial nullable constraint in the V1.0.1 migration
  @Nullable private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role = Role.USER;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<UserIdentity> identities = new HashSet<>();

  @Nullable
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Profile profile;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  @Nullable private Instant deletedAt;

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
  public String getUsername() {
    return this.email;
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
  public boolean isEnabled() {
    return deletedAt == null;
  }

  public void addIdentity(UserIdentity identity) {
    var isProviderPresent =
        identities.stream().anyMatch(i -> i.getProvider().equals(identity.getProvider()));
    if (isProviderPresent) {
      throw new IllegalStateException(
          "User already has identity for provider: " + identity.getProvider());
    }
    identities.add(identity);
    identity.setUser(this);
  }

  public void addProfile(Profile profile) {
    if (this.profile != null) {
      throw new IllegalStateException("User already has a profile assigned.");
    }
    this.profile = profile;
    profile.assignUser(this);
  }

  public void removeIdentityByProvider(AuthProvider authProvider) {
    if (hasPasswordSet() || identities.size() > 1) {
      identities.removeIf(identity -> identity.getProvider() == authProvider);
    } else {
      throw new ProfileOperationNotAllowedException(
          "Cannot remove the last identity without a password set.");
    }
  }

  public boolean hasPasswordSet() {
    return passwordHash != null && !passwordHash.isBlank();
  }
}
