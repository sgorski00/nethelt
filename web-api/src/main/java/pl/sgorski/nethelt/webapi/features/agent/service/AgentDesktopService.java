package pl.sgorski.nethelt.webapi.features.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentConnectionStateChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOnlineEvent;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDesktopService {

  private final AgentRepository agentRepository;
  private final AgentTokenService agentTokenService;
  private final AgentAccessTokenService agentAccessTokenService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public String authenticateAndGenerateToken(String pat) {
    var hashedPat = agentTokenService.hashToken(pat);
    var agent =
        agentRepository.findByHashedToken(hashedPat).orElseThrow(AgentNotFoundException::new);
    if (!agent.isActive()) {
      throw new AgentNotFoundException();
    }
    return agentAccessTokenService.generateAccessToken(agent);
  }

  @Transactional
  public void heartbeat(Long agentId) {
    var agent = agentRepository.findByIdForUpdate(agentId).orElseThrow(AgentNotFoundException::new);
    var isFirstHeartbeat = agent.getLastHeartbeatAt() == null;
    var isOnlineBack = agent.heartbeat();
    var network = agent.getNetwork();
    if (isOnlineBack) {
      log.info("Agent with ID {} is back online", agentId);
      eventPublisher.publishEvent(
          new AgentOnlineEvent(network.getUser().getId(), network.getName(), agent.getName()));
    }
    if (isOnlineBack || isFirstHeartbeat) {
      eventPublisher.publishEvent(
          new AgentConnectionStateChangedEvent(
              network.getUser().getId(),
              network.getId(),
              agent.getId(),
              AgentConnectionState.ONLINE));
    }
  }
}
