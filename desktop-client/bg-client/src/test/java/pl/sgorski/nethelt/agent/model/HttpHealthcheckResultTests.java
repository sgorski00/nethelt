package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.monitoring_result.HttpHealthcheckResult;

public class HttpHealthcheckResultTests {
  @Test
  void construction_shouldCreateHttpHealthcheckResult() {
    var result = new HttpHealthcheckResult(1L, true, "Healthcheck successful", 20L, 200);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertEquals("Healthcheck successful", result.getMessage());
    assertEquals(20L, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
    assertEquals(200, result.getStatusCode());
  }

  @Test
  void construction_shouldAllowNullStatusCodeAndResponseTime() {
    var result = new HttpHealthcheckResult(1L, false, "Connection refused", null, null);

    assertFalse(result.isSuccess());
    assertNull(result.getResponseTimeMs());
    assertNull(result.getStatusCode());
  }
}
