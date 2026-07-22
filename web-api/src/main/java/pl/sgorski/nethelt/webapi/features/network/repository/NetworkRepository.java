package pl.sgorski.nethelt.webapi.features.network.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

public interface NetworkRepository extends JpaRepository<Network, Long> {

  List<Network> findAllByUserIdAndDeletedAtIsNull(Long userId);

  Optional<Network> findByIdAndDeletedAtIsNull(Long id);

  Long countByUserIdAndDeletedAtIsNull(Long userId);

  boolean existsByIdAndUserId(Long networkId, Long userId);

  List<Network> findAllByUserIdAndNameAndDeletedAtIsNotNull(Long userId, String name);
}
