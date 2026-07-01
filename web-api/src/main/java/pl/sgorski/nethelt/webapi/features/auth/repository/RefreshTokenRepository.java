package pl.sgorski.nethelt.webapi.features.auth.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String tokenStr);

  @EntityGraph(attributePaths = "user")
  Optional<RefreshToken> findWithUserByToken(String tokenStr);

  void deleteAllByExpiresAtBeforeOrIsRevokedTrue(Instant now);

  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
  void revokeAllUserTokens(@Param("userId") Long userId);
}
