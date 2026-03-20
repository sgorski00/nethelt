package pl.sgorski.nethelt.webapi.features.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String tokenStr);
    void deleteAllByExpiresAtBeforeOrIsRevokedTrue(Instant now);
}
