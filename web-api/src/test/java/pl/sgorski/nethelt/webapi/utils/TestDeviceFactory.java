package pl.sgorski.nethelt.webapi.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

public final class TestDeviceFactory {

  public static Device createDevice() throws UnknownHostException {
    var network = TestNetworkFactory.createNetwork();
    var name = "Test Device";
    var ip = "127.0.0.1";

    return createDevice(network, name, ip, DeviceType.LAN_CLIENT);
  }

  public static Device createDevice(Network network, String name, String ip, DeviceType type)
      throws UnknownHostException {
    return new Device(network, name, InetAddress.getByName(ip), type);
  }
}
