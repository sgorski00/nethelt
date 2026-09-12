package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NetworkConfigTests {

  @Test
  void construction_shouldCreateNetworkConfig() {
    var config = new NetworkConfig(Operation.PING, true, 60);

    assertEquals(Operation.PING, config.getOperation());
    assertTrue(config.isEnabled());
    assertEquals(60, config.getIntervalSeconds());
  }
}
