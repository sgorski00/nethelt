package pl.sgorski.nethelt.webapi.features.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Entity
@Table(name = "refresh_tokens")
@Data
@ToString(exclude = "user")
@NoArgsConstructor
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private boolean isRevoked = false;

  @Column(nullable = false)
  private Instant expiresAt;

  public RefreshToken(String token, User user, Long expirationTimeInMs) {
    this.token = token;
    this.user = user;
    this.expiresAt = Instant.now().plusMillis(expirationTimeInMs);
  }
}
