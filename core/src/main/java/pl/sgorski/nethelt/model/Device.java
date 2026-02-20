package pl.sgorski.nethelt.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sgorski.nethelt.exception.NetworkException;

@Data
@NoArgsConstructor
public class Device {

  private static final int MAX_PORT = 65535;

  private String name;
  private InetAddress address;
  private Integer port;

  public Device(String name, String ip, Integer port) {
    this.name = name;

    try {
      this.address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      throw new NetworkException("Invalid IP address: " + ip, e);
    }

    if(isValidPort(port)) {
      this.port = port;
    } else {
      throw new NetworkException("Invalid port number: " + port + ". Port must be between 1 and " + MAX_PORT + "." );
    }
  }

  public Device(String name, String ip) {
    this.name = name;
    try {
      this.address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      throw new NetworkException("Invalid IP address: " + ip, e);
    }
    this.port = null;
  }

  private boolean isValidPort(Integer port) {
    return Objects.isNull(port) || (port > 0 && port <= MAX_PORT);
  }
}
