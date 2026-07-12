package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileOperationNotAllowedException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;

@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Entity
@Table(name = "users")
@ToString(exclude = {"passwordHash", "identities"})
@EqualsAndHashCode(exclude = "identities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Column(nullable = false)
  @Getter
  private String email;

  // partial nullable constraint in the V1.0.1 migration
  @Nullable private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Getter
  private Role role = Role.USER;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Getter
  private Set<UserIdentity> identities = new HashSet<>();

  @Nullable
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Getter
  private Profile profile;

  @CreationTimestamp @Getter private Instant createdAt;

  @UpdateTimestamp @Getter private Instant updatedAt;

  @Nullable @Getter private Instant deletedAt;

  public User(String email, String hashedPassword) {
    this.email = email;
    this.passwordHash = hashedPassword;
  }

  public User(String email, AuthProvider provider, String providerId) {
    this.email = email;
    this.addIdentity(provider, providerId);
  }

  public void addIdentity(AuthProvider provider, String providerId) {
    var isProviderPresent = identities.stream().anyMatch(i -> i.getProvider().equals(provider));
    if (isProviderPresent) {
      throw new IllegalStateException("User already has identity for provider: " + provider);
    }
    var identity = new UserIdentity(this, provider, providerId);
    identities.add(identity);
  }

  public void removeIdentityByProvider(AuthProvider authProvider) {
    if (isLocal() || identities.size() > 1) {
      identities.removeIf(identity -> identity.getProvider() == authProvider);
    } else {
      throw new ProfileOperationNotAllowedException(
          "Cannot remove the last identity without a password set.");
    }
  }

  public boolean isLocal() {
    return passwordHash != null && !passwordHash.isBlank();
  }

  public void setPassword(String hashedPassword) {
    this.passwordHash = hashedPassword;
  }

  public void addProfile(Profile profile) {
    if (this.profile != null) {
      throw new ProfileAlreadyExistsException();
    }
    this.profile = profile;
    profile.assignUser(this);
  }

  public void delete() {
    this.deletedAt = Instant.now();
  }

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
  public boolean isEnabled() {
    return deletedAt == null;
  }
}
