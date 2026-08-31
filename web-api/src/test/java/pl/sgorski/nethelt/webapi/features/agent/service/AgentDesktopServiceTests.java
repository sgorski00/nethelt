package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class AgentDesktopServiceTests {

  @Mock private AgentRepository agentRepository;
  @Mock private AgentTokenService agentTokenService;
  @Mock private AgentAccessTokenService agentAccessTokenService;
  @InjectMocks private AgentDesktopService agentDesktopService;

  @Test
  void authenticateAndGenerateToken_shouldThrow_whenAgentNotFound() {
    var pat = "raw-pat";
    when(agentTokenService.hashToken(pat)).thenReturn("hashed-pat");
    when(agentRepository.findByHashedToken("hashed-pat")).thenReturn(Optional.empty());

    assertThrows(
        AgentNotFoundException.class, () -> agentDesktopService.authenticateAndGenerateToken(pat));
  }

  @Test
  void authenticateAndGenerateToken_shouldThrow_whenAgentIsNotActive() {
    var pat = "raw-pat";
    var agent = TestAgentFactory.createAgent();
    agent.deactivate();
    when(agentTokenService.hashToken(pat)).thenReturn("hashed-pat");
    when(agentRepository.findByHashedToken("hashed-pat")).thenReturn(Optional.of(agent));

    assertThrows(
        AgentNotFoundException.class, () -> agentDesktopService.authenticateAndGenerateToken(pat));
  }

  @Test
  void authenticateAndGenerateToken_shouldGenerateAccessToken_whenRequestIsValid() {
    var pat = "raw-pat";
    var agent = TestAgentFactory.createAgent();
    when(agentTokenService.hashToken(pat)).thenReturn("hashed-pat");
    when(agentRepository.findByHashedToken("hashed-pat")).thenReturn(Optional.of(agent));
    when(agentAccessTokenService.generateAccessToken(agent)).thenReturn("access-token");

    var result = agentDesktopService.authenticateAndGenerateToken(pat);

    assertEquals("access-token", result);
  }

  @Test
  void heartbeat_shouldThrow_whenAgentNotFound() {
    when(agentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(AgentNotFoundException.class, () -> agentDesktopService.heartbeat(1L));
  }

  @Test
  void heartbeat_shouldUpdateHeartbeat_whenAgentFound() {
    var agent = TestAgentFactory.createAgent();
    when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

    agentDesktopService.heartbeat(1L);

    assertNotNull(agent.getLastHeartbeatAt());
  }
}
