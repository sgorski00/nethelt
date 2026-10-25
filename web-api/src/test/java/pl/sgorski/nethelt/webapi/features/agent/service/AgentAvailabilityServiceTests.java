package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.domain.AlertState;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOfflineEvent;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class AgentAvailabilityServiceTests {

  private static final Instant LAST_HEARTBEAT = Instant.parse("2026-01-01T12:00:00Z");

  @Mock private AgentRepository agentRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private AgentAvailabilityService agentAvailabilityService;

  @Test
  void findAgentIdsToMarkOffline_shouldReturnOnlyOfflineAgents() {
    var offline = createAgent(1L, LAST_HEARTBEAT);
    var online = createAgent(2L, LAST_HEARTBEAT.plusSeconds(120));
    when(agentRepository.findAllByStatusAndAvailabilityAlertStateAndLastHeartbeatAtIsNotNull(
            AgentStatus.ACTIVE, AlertState.OK))
        .thenReturn(List.of(offline, online));

    var result =
        agentAvailabilityService.findAgentIdsToMarkOffline(LAST_HEARTBEAT.plusSeconds(301));

    assertEquals(List.of(1L), result);
  }

  @Test
  void markOffline_shouldFireAlertAndPublishEvent_whenAgentIsOffline() {
    var agent = createAgent(1L, LAST_HEARTBEAT);
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentAvailabilityService.markOffline(1L, LAST_HEARTBEAT.plusSeconds(301));

    assertEquals(AlertState.FIRING, agent.getAvailabilityAlertState());
    verify(eventPublisher)
        .publishEvent(
            new AgentOfflineEvent(
                null, "Test Network", "Test Agent", LAST_HEARTBEAT, Duration.ofMinutes(5)));
  }

  @Test
  void markOffline_shouldNotPublishEvent_whenHeartbeatArrivedInTheMeantime() {
    var agent = createAgent(1L, LAST_HEARTBEAT.plusSeconds(300));
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentAvailabilityService.markOffline(1L, LAST_HEARTBEAT.plusSeconds(301));

    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void markOffline_shouldNotPublishEvent_whenAlertIsAlreadyFiring() {
    var agent = createAgent(1L, LAST_HEARTBEAT);
    ReflectionTestUtils.setField(agent, "availabilityAlertState", AlertState.FIRING);
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(agent));

    agentAvailabilityService.markOffline(1L, LAST_HEARTBEAT.plusSeconds(301));

    verifyNoInteractions(eventPublisher);
  }

  @Test
  void markOffline_shouldDoNothing_whenAgentNotFound() {
    when(agentRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

    agentAvailabilityService.markOffline(1L, LAST_HEARTBEAT);

    verifyNoInteractions(eventPublisher);
  }

  private Agent createAgent(Long id, Instant lastHeartbeatAt) {
    var agent = TestAgentFactory.createAgentWithId(id, 1L);
    ReflectionTestUtils.setField(agent, "lastHeartbeatAt", lastHeartbeatAt);
    return agent;
  }
}
