package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.test_utils.TestDeviceFactory;

public class TelnetResultTests {
  @Test
  void construction_shouldCreateTelnetResult() {
    var device = TestDeviceFactory.createDeviceWithPort();

    var result = new TelnetResult(device, true, "Telnet successful", 20, true);

    assertSame(device, result.getDevice());
    assertTrue(result.isSuccess());
    assertEquals("Telnet successful", result.getMessage());
    assertEquals(20, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
    assertTrue(result.isPortOpen());
  }
}
