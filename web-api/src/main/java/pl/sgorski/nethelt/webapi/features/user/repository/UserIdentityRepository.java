package pl.sgorski.nethelt.webapi.features.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    boolean existsByProviderAndProviderId(AuthProvider authProvider, String providerId);
}
