package pl.sgorski.nethelt.webapi.utils;

import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

public final class TestAgentFactory {

  public static Agent createAgentWithId(Long id, Long networkId) {
    var network = TestNetworkFactory.createNetwork(networkId);
    var name = "Test Agent";
    var hashedToken = "hashedToken123";

    var agent = createAgent(network, name, hashedToken);
    ReflectionTestUtils.setField(agent, "id", id);
    return agent;
  }

  public static Agent createAgent() {
    var network = TestNetworkFactory.createNetwork();
    var name = "Test Agent";
    var hashedToken = "hashedToken123";

    return createAgent(network, name, hashedToken);
  }

  public static Agent createAgent(Network network) {
    return createAgent(network, "Test Agent", "hashedToken123");
  }

  public static Agent createAgent(String name, String hashedToken) {
    var network = TestNetworkFactory.createNetwork();
    return createAgent(network, name, hashedToken);
  }

  public static Agent createAgent(Network network, String name, String hashedToken) {
    return new Agent(network, name, hashedToken);
  }
}
