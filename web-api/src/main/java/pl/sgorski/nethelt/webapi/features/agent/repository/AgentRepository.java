package pl.sgorski.nethelt.webapi.features.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;

public interface AgentRepository extends JpaRepository<Agent, Long> {}
