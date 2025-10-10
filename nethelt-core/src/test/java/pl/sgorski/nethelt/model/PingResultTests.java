package pl.sgorski.nethelt.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PingResultTests {

  private Device device;
  private PingResult result;

  @BeforeEach
  void setUp() {
    device = new Device("Device1", "192.168.1.1", 8080);
    result = new PingResult(device, true, "Ping successful", 20);
  }

  @Test
  void construction_shouldCreatePingResult() {
    assertEquals(device, result.getDevice());
    assertTrue(result.isSuccess());
    assertEquals("Ping successful", result.getMessage());
    assertEquals(20, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
  }

  @Test
  void setDevice_shouldUpdateDevice() {
    Device newDevice = new Device("Device2", "192.168.1.2", 8081);

    result.setDevice(newDevice);

    assertEquals(newDevice, result.getDevice());
  }

  @Test
  void setSuccess_shouldUpdateSuccess() {
    result.setSuccess(false);

    assertFalse(result.isSuccess());
  }

  @Test
  void setMessage_shouldUpdateMessage() {
    result.setMessage("Ping failed");

    assertEquals("Ping failed", result.getMessage());
  }

  @Test
  void setResponseTimeMs_shouldUpdateResponseTime() {
    result.setResponseTimeMs(50);

    assertEquals(50, result.getResponseTimeMs());
  }

  @Test
  void toString_shouldReturnStringRepresentation() {
    String str = result.toString();

    assertTrue(str.contains("PingResult{Result{device="));
    assertTrue(str.contains("timestamp="));
    assertTrue(str.contains("success=true"));
    assertTrue(str.contains("message='Ping successful'"));
    assertTrue(str.contains("responseTimeMs=20}}"));
  }
}
