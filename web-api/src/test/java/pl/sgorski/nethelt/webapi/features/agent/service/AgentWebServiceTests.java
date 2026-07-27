package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.network.NetworkNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.dto.command.AgentCreateCommand;
import pl.sgorski.nethelt.webapi.features.agent.mapper.AgentUpdateCommand;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.features.network.service.NetworkService;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

@ExtendWith(MockitoExtension.class)
public class AgentWebServiceTests {

  @Mock private AgentRepository agentRepository;
  @Mock private AgentTokenService agentTokenService;
  @Mock private NetworkService networkService;
  @InjectMocks private AgentWebService agentWebService;

  private final AgentCreateCommand createCommand = new AgentCreateCommand(1L, "Agent Name");
  private final AgentUpdateCommand updateCommand = new AgentUpdateCommand(1L, "Agent Updated Name");

  @Test
  void getAgent_shouldReturnAgent_whenFound() {
    var agent = TestAgentFactory.createAgent();
    mockFindByNetworkId(agent);

    var result = agentWebService.getAgent(1L);

    assertSame(agent, result);
  }

  @Test
  void getAgent_shouldThrow_whenNotFound() {
    mockFindByNetworkId(null);

    assertThrows(AgentNotFoundException.class, () -> agentWebService.getAgent(1L));
  }

  @Test
  void createAgentAndRetrieveRawToken_shouldCreateAgentAndReturnRawToken() {
    var network = TestNetworkFactory.createNetwork();
    when(networkService.getNetwork(1L)).thenReturn(network);
    mockTokens("raw-token", "hashed-token");

    var token = agentWebService.createAgentAndRetrieveRawToken(createCommand);

    assertEquals("raw-token", token);
    verify(agentRepository)
        .save(
            argThat(
                agent ->
                    agent.getNetwork().equals(network)
                        && agent.getName().equals("Agent Name")
                        && agent.getHashedToken().equals("hashed-token")));
  }

  @Test
  void createAgentAndRetrieveRawToken_shouldThrow_whenNetworkNotFound() {
    when(networkService.getNetwork(1L)).thenThrow(new NetworkNotFoundException());

    assertThrows(
        NetworkNotFoundException.class,
        () -> agentWebService.createAgentAndRetrieveRawToken(createCommand));
  }

  @Test
  void createAgentAndRetrieveRawToken_shouldThrow_whenNetworkHasAgentAlready() {
    var network = TestNetworkFactory.createNetwork();
    ReflectionTestUtils.setField(network, "agent", TestAgentFactory.createAgent(network));
    when(networkService.getNetwork(1L)).thenReturn(network);

    assertThrows(
        AgentAlreadyExistsException.class,
        () -> agentWebService.createAgentAndRetrieveRawToken(createCommand));
  }

  @Test
  void renewToken_shouldRenewTokenForAgentAndReturnRawToken() {
    var agent = TestAgentFactory.createAgent();
    mockFindByNetworkId(agent);
    mockTokens("some-new-raw-token-123", "some-new-hashed-token-123");

    var token = agentWebService.renewToken(1L);

    assertEquals("some-new-raw-token-123", token);
    assertEquals("some-new-hashed-token-123", agent.getHashedToken());
  }

  @Test
  void renewToken_shouldThrow_whenAgentNotFound() {
    mockFindByNetworkId(null);

    assertThrows(AgentNotFoundException.class, () -> agentWebService.renewToken(1L));
  }

  @Test
  void updateAgent_shouldUpdateAllPossibleFields() {
    var agent = TestAgentFactory.createAgent();
    mockFindByNetworkId(agent);

    var result = agentWebService.updateAgent(updateCommand);

    assertNotNull(result);
    assertEquals("Agent Updated Name", result.getName());
  }

  @Test
  void updateAgent_shouldThrow_whenAgentNotFound() {
    mockFindByNetworkId(null);

    assertThrows(AgentNotFoundException.class, () -> agentWebService.updateAgent(updateCommand));
  }

  @Test
  void changeStatus_shouldChangeToActive_whenActiveIsPassed() {
    var agent = TestAgentFactory.createAgent();
    agent.deactivate();
    mockFindByNetworkId(agent);

    var result = agentWebService.changeStatus(1L, AgentStatus.ACTIVE);

    assertSame(agent, result);
    assertEquals(AgentStatus.ACTIVE, agent.getStatus());
  }

  @Test
  void changeStatus_shouldChangeToDisabled_whenDisabledIsPassed() {
    var agent = TestAgentFactory.createAgent();
    mockFindByNetworkId(agent);

    var result = agentWebService.changeStatus(1L, AgentStatus.DISABLED);

    assertSame(agent, result);
    assertEquals(AgentStatus.DISABLED, agent.getStatus());
  }

  @Test
  void changeStatus_shouldThrow_whenAgentNotFound() {
    mockFindByNetworkId(null);

    assertThrows(
        AgentNotFoundException.class, () -> agentWebService.changeStatus(1L, AgentStatus.ACTIVE));
  }

  @Test
  void deleteAgent_shouldInvokeRepositoryDeletion() {
    agentWebService.deleteAgent(1L);

    verify(agentRepository).deleteByNetworkId(1L);
  }

  private void mockFindByNetworkId(@Nullable Agent agent) {
    when(agentRepository.findByNetworkId(anyLong())).thenReturn(Optional.ofNullable(agent));
  }

  private void mockTokens(String rawToken, String hashedToken) {
    when(agentTokenService.generateRawToken()).thenReturn(rawToken);
    when(agentTokenService.hashToken(rawToken)).thenReturn(hashedToken);
  }
}
