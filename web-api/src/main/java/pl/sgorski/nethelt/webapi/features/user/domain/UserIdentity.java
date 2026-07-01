package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;

@Entity
@Table(
    name = "user_identities",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"provider", "provider_id"}),
      @UniqueConstraint(columnNames = {"user_id", "provider"})
    })
@Data
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor
public class UserIdentity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AuthProvider provider;

  @Column(nullable = false)
  private String providerId;
}
