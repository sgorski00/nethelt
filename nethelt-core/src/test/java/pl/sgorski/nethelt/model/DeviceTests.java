package pl.sgorski.nethelt.model;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.exception.NetworkException;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class DeviceTests {

  @Test
  void constructionWithPort_shouldCreateDevice_WithPort() {
    Device device = new Device("Device1", "192.168.1.1", 8080);

    assertEquals("Device1", device.getName());
    assertEquals("192.168.1.1", device.getAddress().getHostAddress());
    assertEquals(8080, device.getPort());
  }

  @Test
  void constructionWithPort_shouldCreateDevice_WithoutPort() {
    Device device = new Device("Device1", "192.168.1.1", null);

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
    Device device = new Device("Device2", "10.0.0.1");

    assertEquals("Device2", device.getName());
    assertEquals("10.0.0.1", device.getAddress().getHostAddress());
    assertNull(device.getPort());
  }

  @Test
  void constructionWithoutPort_shouldThrowNetworkException_InvalidIp() {
    assertThrows(NetworkException.class, () -> new Device("Device3", "invalid_ip"));
  }

  @Test
  void setName_shouldUpdateDeviceName() {
    Device device = new Device("Device4", "127.0.0.1", 80);
    device.setName("UpdatedDevice");

    assertEquals("UpdatedDevice", device.getName());
  }

  @Test
  void setAddress_shouldUpdateDeviceAddress() throws Exception {
    Device device = new Device("Device5", "192.168.0.1", 22);
    InetAddress newAddress = InetAddress.getByName("10.0.0.2");
    device.setAddress(newAddress);

    assertEquals("10.0.0.2", device.getAddress().getHostAddress());
  }

  @Test
  void setPort_shouldUpdateDevicePort() {
    Device device = new Device("Device", "172.16.0.1", 443);
    device.setPort(8443);

    assertEquals(8443, device.getPort());
  }

  @Test
  void equals_shouldReturnTrue_EqualDevices() {
    Device device1 = new Device("Device", "192.168.1.1", 8080);
    Device device2 = new Device("Device", "192.168.1.1", 8080);

    assertEquals(device1, device2);
  }

  @Test
  void equals_shouldReturnTrue_SameDevices() {
    Device device1 = new Device("Device", "192.168.1.1", 8080);

    assertEquals(device1, device1);
  }

  @Test
  void equals_shouldReturnFalse_NotADevice() {
    Device device1 = new Device("PC", "192.168.1.1", 8080);

    assertNotEquals("Something else", device1);
  }

  @Test
  void equals_shouldReturnFalse_DevicesWithDifferentNames() {
    Device device1 = new Device("PC", "192.168.1.1", 8080);
    Device device2 = new Device("Server", "192.168.1.1", 8080);

    assertNotEquals(device1, device2);
  }

  @Test
  void equals_shouldReturnFalse_DevicesWithDifferentAddresses() {
    Device device1 = new Device("Device", "192.168.1.1", 8080);
    Device device2 = new Device("Device", "10.0.0.1", 8080);

    assertNotEquals(device1, device2);
  }

  @Test
  void equals_shouldReturnFalse_DevicesWithDifferentPorts() {
    Device device1 = new Device("Device", "192.168.1.1", 8080);
    Device device2 = new Device("Device", "192.168.1.1", 9090);

    assertNotEquals(device1, device2);
  }
}
