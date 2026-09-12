package pl.sgorski.nethelt.agent.test_utils;

import pl.sgorski.nethelt.agent.model.Device;

public class TestDeviceFactory {
  public static Device createDeviceWithPort() {
    return new Device("Test Device", "192.168.1.2", 8080);
  }

  public static Device createDeviceWithPort(int port) {
    return new Device("Test Device", "192.168.1.2", port);
  }

  public static Device createDeviceWithoutPort() {
    return new Device("Test Device", "192.168.1.2");
  }
}
