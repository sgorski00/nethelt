package pl.sgorski.nethelt.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkConfigTests {

  private NetworkConfig config;

  @BeforeEach
  void setUp() {
    config = new NetworkConfig(Operation.PING, true, 60);
  }

  @Test
  void construction_shouldCreateNetworkConfig() {
    assertEquals(Operation.PING, config.getOperation());
    assertTrue(config.isEnabled());
    assertEquals(60, config.getIntervalSeconds());
  }

  @Test
  void setEnabled_shouldUpdateEnabled() {
    config.setEnabled(false);

    assertFalse(config.isEnabled());
  }

  @Test
  void setIntervalSeconds_shouldUpdateInterval() {
    config.setIntervalSeconds(120);

    assertEquals(120, config.getIntervalSeconds());
  }

  @Test
  void setOperation_shouldUpdateOperation() {
    config.setOperation(Operation.TELNET);
    assertEquals(Operation.TELNET, config.getOperation());
  }

  @Test
  void toString_shouldReturnStringRepresentation() {
    String expected = "NetworkConfig{operation=PING, enabled=true, intervalSeconds=60}";

    assertEquals(expected, config.toString());
  }
}
