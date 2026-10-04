package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;

public class PingResultTests {

  @Test
  void construction_shouldCreatePingResult() {
    var result = new PingResult(1L, true, "Ping successful", 20L);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertEquals("Ping successful", result.getMessage());
    assertEquals(20L, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
  }
}
