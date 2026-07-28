package pl.sgorski.nethelt.webapi.features.device.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

public class DeviceTests {

  @Test
  void constructor_shouldCreateDeviceWithValidParameters() throws UnknownHostException {
    var network = TestNetworkFactory.createNetwork();
    var ipAddress = Inet4Address.getByName("127.0.0.1");

    var device = new Device(network, "Device1", ipAddress, DeviceType.LAN_CLIENT);

    assertSame(network, device.getNetwork());
    assertEquals(ipAddress, device.getIpAddress());
    assertEquals("Device1", device.getName());
    assertEquals(DeviceType.LAN_CLIENT, device.getType());
    assertTrue(device.isEnabled());
  }

  @Test
  void constructor_shouldThrow_whenAddressIsNotIpv4() throws UnknownHostException {
    var network = TestNetworkFactory.createNetwork();
    var ipAddress = Inet6Address.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

    assertThrows(
        DeviceValidationFailedException.class,
        () -> new Device(network, "Device1", ipAddress, DeviceType.LAN_CLIENT));
  }

  @Test
  void update_shouldUpdateDevice_whenNoNullsPresent() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();
    var updatedAddress = Inet4Address.getByName("192.168.0.16");

    device.update("new-device-name", updatedAddress, DeviceType.NETWORK_DEVICE);

    assertEquals("new-device-name", device.getName());
    assertEquals(updatedAddress, device.getIpAddress());
    assertEquals(DeviceType.NETWORK_DEVICE, device.getType());
  }

  @Test
  void update_shouldUpdateDevice_whenOnlyNameIsPresent() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();

    device.update("new-device-name", null, null);

    assertEquals("new-device-name", device.getName());
    assertNotNull(device.getIpAddress());
    assertNotNull(device.getType());
  }

  @Test
  void update_shouldUpdateDevice_whenOnlyAddressIsPresent() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();
    var updatedAddress = Inet4Address.getByName("192.168.0.16");

    device.update(null, updatedAddress, null);

    assertNotNull(device.getName());
    assertEquals(updatedAddress, device.getIpAddress());
    assertNotNull(device.getType());
  }

  @Test
  void update_shouldThrow_whenAddressIsNotIpv4() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();
    var updatedAddress = Inet6Address.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

    assertThrows(
        DeviceValidationFailedException.class, () -> device.update(null, updatedAddress, null));
  }

  @Test
  void update_shouldUpdateDevice_whenOnlyTypeIsPresent() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();

    device.update(null, null, DeviceType.WIFI_CLIENT);

    assertNotNull(device.getName());
    assertNotNull(device.getIpAddress());
    assertEquals(DeviceType.WIFI_CLIENT, device.getType());
  }

  @Test
  void disable_shouldSetIsEnabledToFalse() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();

    assertTrue(device.isEnabled());
    device.disable();

    assertFalse(device.isEnabled());
  }

  @Test
  void enable_shouldSetIsEnabledToTrue() throws UnknownHostException {
    var device = TestDeviceFactory.createDevice();
    device.disable();

    assertFalse(device.isEnabled());
    device.enable();

    assertTrue(device.isEnabled());
  }
}
