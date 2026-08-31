package pl.sgorski.nethelt.webapi.features.agent.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;

public interface AgentRepository extends JpaRepository<Agent, Long> {
  Optional<Agent> findByNetworkId(Long networkId);

  Optional<Agent> findByHashedToken(String hashedToken);

  void deleteByNetworkId(Long networkId);
}
