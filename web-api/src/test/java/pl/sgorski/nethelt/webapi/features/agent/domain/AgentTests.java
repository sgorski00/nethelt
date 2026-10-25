package pl.sgorski.nethelt.webapi.features.agent.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

public class AgentTests {

  @Test
  void constructor_shouldCreateAgentWithCorrectValues() {
    var network = TestNetworkFactory.createNetwork();

    var agent = new Agent(network, "AgentName", "hashedToken123");

    assertSame(network, agent.getNetwork());
    assertEquals("AgentName", agent.getName());
    assertEquals("hashedToken123", agent.getHashedToken());
    assertNotNull(agent.getTokenCreatedAt());
    assertTrue(agent.isActive());
    assertEquals(AgentConfiguration.defaults(), agent.getConfiguration());
  }

  @Test
  void changeName_shouldUpdateAgentName() {
    var agent = TestAgentFactory.createAgent("Old Name", "hashedToken123");

    agent.changeName("NewName");

    assertEquals("NewName", agent.getName());
  }

  @Test
  void changeToken_shouldUpdateTokenAndCreationTimestamp() {
    var agent = TestAgentFactory.createAgent("AgentName", "hashedToken123");
    var oldTokenCreatedAt = agent.getTokenCreatedAt();

    agent.changeToken("newHashedToken456");

    assertEquals("newHashedToken456", agent.getHashedToken());
    assertTrue(agent.getTokenCreatedAt().isAfter(oldTokenCreatedAt));
  }

  @Test
  void heartbeat_shouldUpdateLastHeartbeatAt() {
    var agent = TestAgentFactory.createAgent();

    assertNull(agent.getLastHeartbeatAt());
    agent.heartbeat();
    assertNotNull(agent.getLastHeartbeatAt());
  }

  @Test
  void heartbeat_shouldReturnFalse_whenAlertIsNotFiring() {
    var agent = TestAgentFactory.createAgent();

    assertFalse(agent.heartbeat());
    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
  }

  @Test
  void heartbeat_shouldResolveAlertAndReturnTrue_whenAlertIsFiring() {
    var agent = TestAgentFactory.createAgent();
    ReflectionTestUtils.setField(agent, "availabilityAlertState", AlertState.FIRING);

    assertTrue(agent.heartbeat());
    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
  }

  @Test
  void fireAvailabilityAlertIfOffline_shouldFire_whenActiveAgentIsOffline() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));

    var fired = agent.fireAvailabilityAlertIfOffline(Instant.parse("2026-01-01T12:05:01Z"));

    assertTrue(fired);
    assertEquals(AlertState.FIRING, agent.getAvailabilityAlertState());
  }

  @Test
  void fireAvailabilityAlertIfOffline_shouldNotFire_whenAgentIsOnline() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));

    var fired = agent.fireAvailabilityAlertIfOffline(Instant.parse("2026-01-01T12:05:00Z"));

    assertFalse(fired);
    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
  }

  @Test
  void fireAvailabilityAlertIfOffline_shouldNotFire_whenAgentNeverConnected() {
    var agent = TestAgentFactory.createAgent();

    assertFalse(agent.fireAvailabilityAlertIfOffline(Instant.now()));
    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
  }

  @Test
  void fireAvailabilityAlertIfOffline_shouldNotFire_whenAgentIsDisabled() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));
    agent.deactivate();

    var fired = agent.fireAvailabilityAlertIfOffline(Instant.parse("2026-01-01T13:00:00Z"));

    assertFalse(fired);
    assertEquals(AlertState.OK, agent.getAvailabilityAlertState());
  }

  @Test
  void fireAvailabilityAlertIfOffline_shouldNotFireAgain_whenAlertIsAlreadyFiring() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));
    var now = Instant.parse("2026-01-01T13:00:00Z");
    agent.fireAvailabilityAlertIfOffline(now);

    assertFalse(agent.fireAvailabilityAlertIfOffline(now));
    assertEquals(AlertState.FIRING, agent.getAvailabilityAlertState());
  }

  @Test
  void deactivate_shouldSetStatusToDisabled() {
    var agent = TestAgentFactory.createAgent();

    agent.deactivate();

    assertFalse(agent.isActive());
  }

  @Test
  void activate_shouldSetStatusToActive() {
    var agent = TestAgentFactory.createAgent();
    agent.deactivate();

    agent.activate();

    assertTrue(agent.isActive());
  }

  @Test
  void changeConfiguration_shouldReplaceConfiguration() {
    var agent = TestAgentFactory.createAgent();
    var configuration = new AgentConfiguration(Duration.ofMinutes(15));

    agent.changeConfiguration(configuration);

    assertEquals(configuration, agent.getConfiguration());
  }

  @Test
  void connectionState_shouldReturnNeverConnected_whenNoHeartbeatReceived() {
    var agent = TestAgentFactory.createAgent();

    assertEquals(AgentConnectionState.NEVER_CONNECTED, agent.connectionState(Instant.now()));
  }

  @Test
  void connectionState_shouldReturnOnline_whenHeartbeatWithinMaxUnavailabilityTime() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));

    var state = agent.connectionState(Instant.parse("2026-01-01T12:04:59Z"));

    assertEquals(AgentConnectionState.ONLINE, state);
  }

  @Test
  void connectionState_shouldReturnOnline_whenExactlyAtMaxUnavailabilityTime() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));

    var state = agent.connectionState(Instant.parse("2026-01-01T12:05:00Z"));

    assertEquals(AgentConnectionState.ONLINE, state);
  }

  @Test
  void connectionState_shouldReturnOffline_whenHeartbeatOlderThanMaxUnavailabilityTime() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));

    var state = agent.connectionState(Instant.parse("2026-01-01T12:05:01Z"));

    assertEquals(AgentConnectionState.OFFLINE, state);
  }

  @Test
  void connectionState_shouldUseConfiguredMaxUnavailabilityTime() {
    var agent = createAgentWithLastHeartbeat(Instant.parse("2026-01-01T12:00:00Z"));
    agent.changeConfiguration(new AgentConfiguration(Duration.ofMinutes(10)));

    var state = agent.connectionState(Instant.parse("2026-01-01T12:07:00Z"));

    assertEquals(AgentConnectionState.ONLINE, state);
  }

  private Agent createAgentWithLastHeartbeat(Instant lastHeartbeatAt) {
    var agent = TestAgentFactory.createAgent();
    ReflectionTestUtils.setField(agent, "lastHeartbeatAt", lastHeartbeatAt);
    return agent;
  }
}
