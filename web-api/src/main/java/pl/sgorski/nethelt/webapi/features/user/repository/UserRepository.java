package pl.sgorski.nethelt.webapi.features.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
