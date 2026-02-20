package pl.sgorski.nethelt.model;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.exception.NetworkException;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class DeviceTests {

  @Test
  void constructionEmpty_shouldCreateDevice() {
    var device = new Device();

    assertNotNull(device, "Device should contain empty constructor for deserializing!");
  }

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

  @Test
  void setName_shouldUpdateDeviceName() {
    var device = new Device("Device4", "127.0.0.1", 80);
    device.setName("UpdatedDevice");

    assertEquals("UpdatedDevice", device.getName());
  }

  @Test
  void setAddress_shouldUpdateDeviceAddress() throws Exception {
    var device = new Device("Device5", "192.168.0.1", 22);
    InetAddress newAddress = InetAddress.getByName("10.0.0.2");
    device.setAddress(newAddress);

    assertEquals("10.0.0.2", device.getAddress().getHostAddress());
  }

  @Test
  void setPort_shouldUpdateDevicePort() {
    var device = new Device("Device", "172.16.0.1", 443);
    device.setPort(8443);

    assertEquals(8443, device.getPort());
  }

  @Test
  void equals_shouldReturnTrue_EqualDevices() {
    var device1 = new Device("Device", "192.168.1.1", 8080);
    var device2 = new Device("Device", "192.168.1.1", 8080);

    assertEquals(device1, device2);
  }

  @Test
  void equals_shouldReturnTrue_SameDevices() {
    var device1 = new Device("Device", "192.168.1.1", 8080);

    assertEquals(device1, device1);
  }

  @Test
  void equals_shouldReturnFalse_NotADevice() {
    var device1 = new Device("PC", "192.168.1.1", 8080);

    assertNotEquals("Something else", device1);
  }

  @Test
  void equals_shouldReturnFalse_DevicesWithDifferentNames() {
    var device1 = new Device("PC", "192.168.1.1", 8080);
    var device2 = new Device("Server", "192.168.1.1", 8080);

    assertNotEquals(device1, device2);
  }

  @Test
  void equals_shouldReturnFalse_DevicesWithDifferentAddresses() {
    var device1 = new Device("Device", "192.168.1.1", 8080);
    var device2 = new Device("Device", "10.0.0.1", 8080);

    assertNotEquals(device1, device2);
  }

  @Test
  void equals_shouldReturnFalse_DevicesWithDifferentPorts() {
    var device1 = new Device("Device", "192.168.1.1", 8080);
    var device2 = new Device("Device", "192.168.1.1", 9090);

    assertNotEquals(device1, device2);
  }

  @Test
  void toString_shouldReturnCorrectData_WithPort() {
    var device = new Device("Server", "192.168.1.1", 8080);
    var expected = "Device{name='Server', ipv4Address=192.168.1.1, port=8080}";
    assertEquals(expected, device.toString());
  }

  @Test
  void toString_shouldReturnCorrectData_WithoutPort() {
    var device = new Device("Server", "192.168.1.1");
    var expected = "Device{name='Server', ipv4Address=192.168.1.1, port=null}";
    assertEquals(expected, device.toString());
  }

  @Test
  void hashCode_shouldBeConsistent_ForEqualDevices() {
    var device1 = new Device("Device", "192.168.1.1", 8080);
    var device2 = new Device("Device", "192.168.1.1", 8080);

    assertEquals(device1.hashCode(), device2.hashCode());
  }

  @Test
  void hashCode_shouldNotBeConsistent_ForEqualDevices() {
    var device1 = new Device("Device", "192.168.1.1", 8080);
    var device2 = new Device("Device", "192.168.1.2", 8080);

    assertNotEquals(device1.hashCode(), device2.hashCode());
  }
}
