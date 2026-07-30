package pl.sgorski.nethelt.webapi.utils;

import java.net.Inet4Address;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

public final class TestDeviceFactory {

  public static Device createDevice() {
    var network = TestNetworkFactory.createNetwork();
    var name = "Test Device";
    var ip = "127.0.0.1";

    return createDevice(network, name, ip, DeviceType.LAN_CLIENT);
  }

  public static Device createDevice(String name, String ip) {
    var network = TestNetworkFactory.createNetwork();

    return createDevice(network, name, ip, DeviceType.LAN_CLIENT);
  }

  public static Device createDevice(Network network, String name, String ip, DeviceType type) {
    return new Device(network, name, Inet4Address.ofLiteral(ip), type);
  }
}
