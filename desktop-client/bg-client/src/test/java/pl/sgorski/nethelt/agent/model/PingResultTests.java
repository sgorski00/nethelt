package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.test_utils.TestDeviceFactory;

public class PingResultTests {

  @Test
  void construction_shouldCreatePingResult() {
    var device = TestDeviceFactory.createDeviceWithoutPort();

    var result = new PingResult(device, true, "Ping successful", 20);

    assertSame(device, result.getDevice());
    assertTrue(result.isSuccess());
    assertEquals("Ping successful", result.getMessage());
    assertEquals(20, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
  }
}
