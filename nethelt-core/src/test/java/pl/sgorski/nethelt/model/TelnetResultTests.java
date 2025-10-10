package pl.sgorski.nethelt.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TelnetResultTests {

  private Device device;
  private TelnetResult result;

  @BeforeEach
  void setUp() {
    device = new Device("Device1", "192.168.1.1", 8080);
    result = new TelnetResult(device, true, "Telnet successful", 20, true);
  }

  @Test
  void construction_shouldCreateTelnetResult() {
    assertEquals(device, result.getDevice());
    assertTrue(result.isSuccess());
    assertEquals("Telnet successful", result.getMessage());
    assertEquals(20, result.getResponseTimeMs());
    assertNotNull(result.getTimestamp());
    assertTrue(result.isPortOpen());
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
    result.setMessage("Telnet failed");

    assertEquals("Telnet failed", result.getMessage());
  }

  @Test
  void setResponseTimeMs_shouldUpdateResponseTime() {
    result.setResponseTimeMs(50);

    assertEquals(50, result.getResponseTimeMs());
  }

  @Test
  void setPortOpen_shouldUpdatePortOpen() {
    result.setPortOpen(false);

    assertFalse(result.isPortOpen());
  }

  @Test
  void toString_shouldReturnStringRepresentation() {
    String str = result.toString();

    assertTrue(str.contains("TelnetResult{"));
    assertTrue(str.contains("portOpen=true"));
    assertTrue(str.contains("Result{device="));
    assertTrue(str.contains("timestamp="));
    assertTrue(str.contains("success=true"));
    assertTrue(str.contains("message='Telnet successful'"));
    assertTrue(str.contains("responseTimeMs=20}}"));
  }
}
