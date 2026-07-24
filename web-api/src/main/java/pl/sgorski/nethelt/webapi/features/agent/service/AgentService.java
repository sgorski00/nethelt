package pl.sgorski.nethelt.webapi.features.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.dto.command.AgentCreateCommand;
import pl.sgorski.nethelt.webapi.features.agent.mapper.AgentUpdateCommand;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.features.network.service.NetworkService;

@Service
@RequiredArgsConstructor
public class AgentService {

  private final AgentRepository agentRepository;
  private final AgentTokenService agentTokenService;
  private final NetworkService networkService;

  public Agent getAgent(Long networkId) {
    return agentRepository.findByNetworkId(networkId).orElseThrow(AgentNotFoundException::new);
  }

  @Transactional
  public String createAgentAndRetrieveRawToken(AgentCreateCommand command) {
    var network = networkService.getNetwork(command.networkId());
    if (network.getAgent() != null) {
      throw new AgentAlreadyExistsException();
    }
    var token = agentTokenService.generateRawToken();
    var hashedToken = agentTokenService.hashToken(token);
    var agent = new Agent(network, command.name(), hashedToken);
    agentRepository.save(agent);
    return token;
  }

  @Transactional
  public String renewToken(Long networkId) {
    var agent = getAgent(networkId);
    var token = agentTokenService.generateRawToken();
    var hashedToken = agentTokenService.hashToken(token);
    agent.changeToken(hashedToken);
    return token;
  }

  @Transactional
  public Agent updateAgent(AgentUpdateCommand command) {
    var agent = getAgent(command.networkId());
    agent.changeName(command.name());
    return agent;
  }

  @Transactional
  public Agent changeStatus(Long networkId, AgentStatus status) {
    var agent = getAgent(networkId);
    switch (status) {
      case ACTIVE -> agent.activate();
      case DISABLED -> agent.deactivate();
    }
    return agent;
  }

  @Transactional
  public void heartbeat(Long networkId) {
    var agent = getAgent(networkId);
    agent.heartbeat();
  }

  @Transactional
  public void deleteAgent(Long networkId) {
    agentRepository.deleteByNetworkId(networkId);
  }
}
