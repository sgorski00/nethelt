package pl.sgorski.nethelt.webapi.utils;

import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

public final class TestAgentFactory {

  public static Agent createAgent() {
    var network = TestNetworkFactory.createNetwork();
    var name = "Test Agent";
    var hashedToken = "hashedToken123";

    return createAgent(network, name, hashedToken);
  }

  public static Agent createAgent(String name, String hashedToken) {
    var network = TestNetworkFactory.createNetwork();
    return createAgent(network, name, hashedToken);
  }

  public static Agent createAgent(Network network, String name, String hashedToken) {
    return new Agent(network, name, hashedToken);
  }
}
