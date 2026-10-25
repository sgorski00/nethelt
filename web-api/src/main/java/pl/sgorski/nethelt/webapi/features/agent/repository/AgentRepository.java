package pl.sgorski.nethelt.webapi.features.agent.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.domain.AlertState;

public interface AgentRepository extends JpaRepository<Agent, Long> {
  Optional<Agent> findByNetworkId(Long networkId);

  Optional<Agent> findByHashedToken(String hashedToken);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM Agent a WHERE a.id = :id")
  Optional<Agent> findByIdForUpdate(@Param("id") Long id);

  List<Agent> findAllByStatusAndAvailabilityAlertStateAndLastHeartbeatAtIsNotNull(
      AgentStatus status, AlertState availabilityAlertState);

  void deleteByNetworkId(Long networkId);
}
