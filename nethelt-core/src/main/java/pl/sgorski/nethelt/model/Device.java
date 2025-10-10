package pl.sgorski.nethelt.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import pl.sgorski.nethelt.exception.NetworkException;

public class Device {

  private static final int MAX_PORT = 65535;

  private String name;
  private InetAddress address;
  private Integer port;

  public Device() { } //empty constructor for deserializing - DO NOT REMOVE

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

  public String getName() {
    return name;
  }

  public InetAddress getAddress() {
    return address;
  }

  public Integer getPort() {
    return port;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAddress(InetAddress address) {
    this.address = address;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  private boolean isValidPort(Integer port) {
    return Objects.isNull(port) || (port > 0 && port <= MAX_PORT);
  }

  @Override
  public String toString() {
    return "Device{" + "name='" + name + '\'' + ", ipv4Address=" + address.getHostAddress() + ", port=" + port + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Device)) return false;
    Device device = (Device) o;
    return Objects.equals(name, device.name) &&
      Objects.equals(address, device.address) &&
      Objects.equals(port, device.port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, address, port);
  }
}
