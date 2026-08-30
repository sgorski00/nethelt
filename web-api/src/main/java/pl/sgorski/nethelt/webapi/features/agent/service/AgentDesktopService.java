package pl.sgorski.nethelt.webapi.features.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;

@Service
@RequiredArgsConstructor
public class AgentDesktopService {

  private final AgentRepository agentRepository;
  private final AgentTokenService agentTokenService;
  private final AgentAccessTokenService agentAccessTokenService;

  @Transactional(readOnly = true)
  public String authenticateAndGenerateToken(String pat) {
    var hashedPat = agentTokenService.hashToken(pat);
    var agent =
        agentRepository.findByHashedToken(hashedPat).orElseThrow(AgentNotFoundException::new);
    if (agent.isActive()) {
      throw new AgentNotFoundException();
    }
    return agentAccessTokenService.generateAccessToken(agent);
  }

  @Transactional
  public void heartbeat(Long agentId) {
    var agent = agentRepository.findById(agentId).orElseThrow(AgentNotFoundException::new);
    agent.heartbeat();
  }
}
