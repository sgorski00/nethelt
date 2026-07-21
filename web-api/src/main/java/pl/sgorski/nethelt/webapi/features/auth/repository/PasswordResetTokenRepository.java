package pl.sgorski.nethelt.webapi.features.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.auth.domain.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  @EntityGraph(attributePaths = "user")
  Optional<PasswordResetToken> findWithUserByToken(String token);
}
