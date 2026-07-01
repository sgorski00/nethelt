package pl.sgorski.nethelt.webapi.features.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
  boolean existsByProviderAndProviderId(AuthProvider authProvider, String providerId);

  @EntityGraph(attributePaths = "user")
  Optional<UserIdentity> findWithUserByProviderAndProviderId(
      AuthProvider provider, String providerId);
}
