package pl.sgorski.nethelt.webapi.features.agent.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
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
    assertEquals(AgentStatus.ACTIVE, agent.getStatus());
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
  void isOnline_shouldReturnFalse_whenHeartbeatNotRecordedYet() {
    var agent = TestAgentFactory.createAgent();

    var result = agent.isOnline(60);

    assertFalse(result);
  }

  @Test
  void isOnline_shouldReturnFalse_whenHeartbeatIsAfterTimeout() throws InterruptedException {
    var agent = TestAgentFactory.createAgent();
    agent.heartbeat();
    Thread.sleep(1001);

    var result = agent.isOnline(1);

    assertFalse(result);
  }

  @Test
  void isOnline_shouldReturnTrue_whenHeartbeatIsBeforeTimeout() throws InterruptedException {
    var agent = TestAgentFactory.createAgent();
    agent.heartbeat();

    var result = agent.isOnline(60);

    assertTrue(result);
  }

  @Test
  void deactivate_shouldSetStatusToDisabled() {
    var agent = TestAgentFactory.createAgent();

    agent.deactivate();

    assertEquals(AgentStatus.DISABLED, agent.getStatus());
  }

  @Test
  void activate_shouldSetStatusToActive() {
    var agent = TestAgentFactory.createAgent();
    agent.deactivate();

    agent.activate();

    assertEquals(AgentStatus.ACTIVE, agent.getStatus());
  }
}
