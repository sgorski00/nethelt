package pl.sgorski.nethelt.webapi.features.device.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

public class DeviceTests {

  @Test
  void constructor_shouldCreateDeviceWithValidParameters() {
    var network = TestNetworkFactory.createNetwork();
    var ipAddress = Inet4Address.ofLiteral("127.0.0.1");

    var device = new Device(network, "Device1", ipAddress, DeviceType.LAN_CLIENT);

    assertSame(network, device.getNetwork());
    assertEquals(ipAddress, device.getIpAddress());
    assertEquals("Device1", device.getName());
    assertEquals(DeviceType.LAN_CLIENT, device.getType());
    assertTrue(device.isEnabled());
  }

  @Test
  void update_shouldUpdateDevice_whenNoNullsPresent() {
    var device = TestDeviceFactory.createDevice();
    var updatedAddress = Inet4Address.ofLiteral("192.168.0.16");

    device.update("new-device-name", updatedAddress, DeviceType.NETWORK_DEVICE);

    assertEquals("new-device-name", device.getName());
    assertEquals(updatedAddress, device.getIpAddress());
    assertEquals(DeviceType.NETWORK_DEVICE, device.getType());
  }

  @Test
  void update_shouldUpdateDevice_whenOnlyNameIsPresent() {
    var device = TestDeviceFactory.createDevice();

    device.update("new-device-name", null, null);

    assertEquals("new-device-name", device.getName());
    assertNotNull(device.getIpAddress());
    assertNotNull(device.getType());
  }

  @Test
  void update_shouldUpdateDevice_whenOnlyAddressIsPresent() {
    var device = TestDeviceFactory.createDevice();
    var updatedAddress = Inet4Address.ofLiteral("192.168.0.16");

    device.update(null, updatedAddress, null);

    assertNotNull(device.getName());
    assertEquals(updatedAddress, device.getIpAddress());
    assertNotNull(device.getType());
  }

  @Test
  void update_shouldUpdateDevice_whenOnlyTypeIsPresent() {
    var device = TestDeviceFactory.createDevice();

    device.update(null, null, DeviceType.WIFI_CLIENT);

    assertNotNull(device.getName());
    assertNotNull(device.getIpAddress());
    assertEquals(DeviceType.WIFI_CLIENT, device.getType());
  }

  @Test
  void disable_shouldSetIsEnabledToFalse() {
    var device = TestDeviceFactory.createDevice();

    assertTrue(device.isEnabled());
    device.disable();

    assertFalse(device.isEnabled());
  }

  @Test
  void enable_shouldSetIsEnabledToTrue() {
    var device = TestDeviceFactory.createDevice();
    device.disable();

    assertFalse(device.isEnabled());
    device.enable();

    assertTrue(device.isEnabled());
  }
}
