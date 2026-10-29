package pl.sgorski.nethelt.agent.network.discovery;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;

@Slf4j
@Component
public class LocalSubnetResolver {

  // Any address outside local subnets works, the OS only looks up the route to it. 192.0.2.1 is
  // reserved for documentation (TEST-NET-1, RFC 5737), so it is never a real host. The port is
  // irrelevant, connect just requires one.
  private static final InetSocketAddress DEFAULT_ROUTE_LOOKUP_ADDRESS =
      new InetSocketAddress(Inet4Address.ofLiteral("192.0.2.1"), 53);

  public IpRange resolve() {
    var interfaceAddress =
        defaultRouteAddress()
            .or(this::firstInterfaceAddress)
            .orElseThrow(() -> new NetworkException("No IPv4 network interface found"));
    var address = (Inet4Address) interfaceAddress.getAddress();
    log.info(
        "Resolved local address {}/{}",
        address.getHostAddress(),
        interfaceAddress.getNetworkPrefixLength());
    return IpRange.hostsOf(address, interfaceAddress.getNetworkPrefixLength());
  }

  // Finds the interface that leads to the default gateway, so the LAN is scanned and not e.g.
  // a Docker bridge. Connecting a UDP socket sends nothing, it only makes the OS pick the source
  // address from its routing table.
  private Optional<InterfaceAddress> defaultRouteAddress() {
    try (var socket = new DatagramSocket()) {
      socket.connect(DEFAULT_ROUTE_LOOKUP_ADDRESS);
      if (!(socket.getLocalAddress() instanceof Inet4Address local) || local.isAnyLocalAddress()) {
        return Optional.empty();
      }
      var networkInterface = NetworkInterface.getByInetAddress(local);
      if (networkInterface == null) {
        return Optional.empty();
      }
      return networkInterface.getInterfaceAddresses().stream()
          .filter(interfaceAddress -> local.equals(interfaceAddress.getAddress()))
          .findFirst();
    } catch (IOException | UncheckedIOException e) {
      log.debug("Could not find the address of the default route: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<InterfaceAddress> firstInterfaceAddress() {
    try {
      return NetworkInterface.networkInterfaces()
          .filter(LocalSubnetResolver::isUsable)
          .flatMap(networkInterface -> networkInterface.getInterfaceAddresses().stream())
          .filter(interfaceAddress -> interfaceAddress.getAddress() instanceof Inet4Address)
          .findFirst();
    } catch (SocketException e) {
      log.debug("Could not list network interfaces: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private static boolean isUsable(NetworkInterface networkInterface) {
    try {
      return networkInterface.isUp()
          && !networkInterface.isLoopback()
          && !networkInterface.isVirtual()
          && !networkInterface.isPointToPoint();
    } catch (SocketException e) {
      return false;
    }
  }
}
