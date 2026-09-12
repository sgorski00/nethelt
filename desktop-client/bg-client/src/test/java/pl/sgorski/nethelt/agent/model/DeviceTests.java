package pl.sgorski.nethelt.agent.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.exception.NetworkException;

class DeviceTests {

  @Test
  void constructionWithPort_shouldCreateDevice_WithPort() {
    var device = new Device("Device1", "192.168.1.1", 8080);

    assertEquals("Device1", device.getName());
    assertEquals("192.168.1.1", device.getAddress().getHostAddress());
    assertEquals(8080, device.getPort());
  }

  @Test
  void constructionWithPort_shouldCreateDevice_WithoutPort() {
    var device = new Device("Device1", "192.168.1.1", null);

    assertEquals("Device1", device.getName());
    assertEquals("192.168.1.1", device.getAddress().getHostAddress());
    assertNull(device.getPort());
  }

  @Test
  void constructionWithPort_shouldThrowNetworkException_InvalidIp() {
    assertThrows(NetworkException.class, () -> new Device("Device3", "invalid_ip", 8080));
  }

  @Test
  void constructionWithPort_shouldThrowNetworkException_InvalidPortLessThan1() {
    assertThrows(NetworkException.class, () -> new Device("Device3", "192.168.1.1", 0));
  }

  @Test
  void constructionWithPort_shouldThrowNetworkException_InvalidPortGreaterThanMax() {
    assertThrows(NetworkException.class, () -> new Device("Device3", "192.168.1.1", 65536));
  }

  @Test
  void constructionWithoutPort_shouldCreateDevice() {
    var device = new Device("Device2", "10.0.0.1");

    assertEquals("Device2", device.getName());
    assertEquals("10.0.0.1", device.getAddress().getHostAddress());
    assertNull(device.getPort());
  }

  @Test
  void constructionWithoutPort_shouldThrowNetworkException_InvalidIp() {
    assertThrows(NetworkException.class, () -> new Device("Device3", "invalid_ip"));
  }
}
