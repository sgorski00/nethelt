package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;

@Entity
@Table(
    name = "user_identities",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"provider", "provider_id"}),
      @UniqueConstraint(columnNames = {"user_id", "provider"})
    })
@Getter
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @Nullable
  private User user;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AuthProvider provider;

  @Column(nullable = false)
  private String providerId;

  public UserIdentity(User user, AuthProvider provider, String providerId) {
    this.user = user;
    this.provider = provider;
    this.providerId = providerId;
  }
}
