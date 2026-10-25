package pl.sgorski.nethelt.webapi.features.agent.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.domain.AlertState;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentConnectionStateChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOfflineEvent;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentAvailabilityService {

  private final AgentRepository agentRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public List<Long> findAgentIdsToMarkOffline(Instant now) {
    return agentRepository
        .findAllByStatusAndAvailabilityAlertStateAndLastHeartbeatAtIsNotNull(
            AgentStatus.ACTIVE, AlertState.OK)
        .stream()
        .filter(agent -> agent.connectionState(now) == AgentConnectionState.OFFLINE)
        .map(Agent::getId)
        .toList();
  }

  /**
   * Locks the agent and fires its availability alert if it is still offline, so a heartbeat
   * received in the meantime wins and no notification is sent.
   */
  @Transactional
  public void markOffline(Long agentId, Instant now) {
    agentRepository
        .findByIdForUpdate(agentId)
        .filter(agent -> agent.fireAvailabilityAlertIfOffline(now))
        .ifPresent(
            agent -> {
              log.info("Agent with ID {} is offline", agent.getId());
              var network = agent.getNetwork();
              eventPublisher.publishEvent(
                  new AgentOfflineEvent(
                      network.getUser().getId(),
                      network.getName(),
                      agent.getName(),
                      agent.getLastHeartbeatAt(),
                      agent.getConfiguration().getMaxUnavailabilityTime()));
              eventPublisher.publishEvent(
                  new AgentConnectionStateChangedEvent(
                      network.getUser().getId(),
                      network.getId(),
                      agent.getId(),
                      AgentConnectionState.OFFLINE));
            });
  }
}
