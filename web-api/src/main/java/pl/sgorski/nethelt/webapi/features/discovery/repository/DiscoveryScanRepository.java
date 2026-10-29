package pl.sgorski.nethelt.webapi.features.discovery.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;

public interface DiscoveryScanRepository extends JpaRepository<DiscoveryScan, Long> {
  @EntityGraph(attributePaths = "results")
  Optional<DiscoveryScan> findByNetworkId(Long networkId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM DiscoveryScan s WHERE s.network.id = :networkId")
  Optional<DiscoveryScan> findByNetworkIdForUpdate(@Param("networkId") Long networkId);
}
