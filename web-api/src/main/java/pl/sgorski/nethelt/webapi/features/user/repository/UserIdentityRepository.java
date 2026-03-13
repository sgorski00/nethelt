package pl.sgorski.nethelt.webapi.features.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;

import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    boolean existsByProviderAndProviderId(AuthProvider authProvider, String providerId);

    @EntityGraph(attributePaths = "user")
    Optional<UserIdentity> findWithUserByProviderAndProviderId(AuthProvider provider, String providerId);
}
