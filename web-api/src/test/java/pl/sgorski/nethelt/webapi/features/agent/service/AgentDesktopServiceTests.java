package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.domain.AlertState;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentConnectionStateChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOnlineEvent;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class AgentDesktopServiceTests {

  @Mock private AgentRepository agentRepository;
  @Mock private AgentTokenService agentTokenService;
  @Mock private AgentAccessTokenService agentAccessTokenService;
  @Mock private ApplicationEventPublisher eventPublisher;
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
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

    assertThrows(AgentNotFoundException.class, () -> agentDesktopService.heartbeat(1L));
  }

  @Test
  void heartbeat_shouldPublishOnlineState_whenAgentConnectsForTheFirstTime() {
    var agent = TestAgentFactory.createAgentWithId(1L, 2L);
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentDesktopService.heartbeat(1L);

    assertNotNull(agent.getLastHeartbeatAt());
    verify(eventPublisher)
        .publishEvent(
            new AgentConnectionStateChangedEvent(null, 2L, 1L, AgentConnectionState.ONLINE));
    verify(eventPublisher, never()).publishEvent(any(AgentOnlineEvent.class));
  }

  @Test
  void heartbeat_shouldNotPublishEvents_whenAgentIsAlreadyOnline() {
    var agent = TestAgentFactory.createAgentWithId(1L, 2L);
    ReflectionTestUtils.setField(agent, "lastHeartbeatAt", Instant.now());
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentDesktopService.heartbeat(1L);

    verifyNoInteractions(eventPublisher);
  }

  @Test
  void heartbeat_shouldNotPublishEvents_whenOfflineAlertWasNotFiredYet() {
    var agent = TestAgentFactory.createAgentWithId(1L, 2L);
    ReflectionTestUtils.setField(agent, "lastHeartbeatAt", Instant.now().minusSeconds(3600));
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentDesktopService.heartbeat(1L);

    verifyNoInteractions(eventPublisher);
  }

  @Test
  void heartbeat_shouldPublishOnlineEventAndState_whenAvailabilityAlertWasFiring() {
    var agent = TestAgentFactory.createAgentWithId(1L, 2L);
    ReflectionTestUtils.setField(agent, "lastHeartbeatAt", Instant.now().minusSeconds(3600));
    ReflectionTestUtils.setField(agent, "availabilityAlertState", AlertState.FIRING);
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentDesktopService.heartbeat(1L);

    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
    verify(eventPublisher).publishEvent(new AgentOnlineEvent(null, "Test Network", "Test Agent"));
    verify(eventPublisher)
        .publishEvent(
            new AgentConnectionStateChangedEvent(null, 2L, 1L, AgentConnectionState.ONLINE));
  }
}
