package pl.sgorski.nethelt.webapi.features.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
  @EntityGraph(attributePaths = "user")
  Optional<Profile> findWithUserByUserId(Long userId);
}
