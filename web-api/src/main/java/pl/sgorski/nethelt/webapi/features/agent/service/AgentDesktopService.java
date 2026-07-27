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

  @Transactional
  public void heartbeat(Long agentId) {
    var agent = agentRepository.findById(agentId).orElseThrow(AgentNotFoundException::new);
    agent.heartbeat();
  }
}
