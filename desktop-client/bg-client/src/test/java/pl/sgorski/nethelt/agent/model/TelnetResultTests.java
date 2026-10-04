package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;

public class TelnetResultTests {
  @Test
  void construction_shouldCreateTelnetResult() {
    var result = new TelnetResult(1L, true, "Telnet successful", 20L, true);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertEquals("Telnet successful", result.getMessage());
    assertEquals(20L, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
    assertTrue(result.isPortOpen());
  }
}
