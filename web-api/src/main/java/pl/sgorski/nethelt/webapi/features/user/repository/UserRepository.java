package pl.sgorski.nethelt.webapi.features.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    @EntityGraph(attributePaths = {"identities", "profile"})
    Optional<User> findWithIdentitiesAndProfileByIdAndDeletedAtIsNull(Long id);
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
}
