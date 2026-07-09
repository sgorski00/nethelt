package pl.sgorski.nethelt.webapi.features.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Entity
@Table(name = "refresh_tokens")
@ToString(exclude = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @Getter
  private String token;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @Getter
  private User user;

  @Column(nullable = false)
  private boolean isRevoked = false;

  @Column(nullable = false)
  private Instant expiresAt;

  public RefreshToken(User user, Instant expiresAt) {
    this.token = UUID.randomUUID().toString();
    this.user = user;
    this.expiresAt = expiresAt;
  }

  public boolean isValid() {
    return !isRevoked && expiresAt.isAfter(Instant.now());
  }

  public void revoke() {
    this.isRevoked = true;
  }
}
