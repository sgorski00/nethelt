package pl.sgorski.nethelt.webapi.features.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);
}
