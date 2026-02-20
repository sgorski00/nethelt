package pl.sgorski.nethelt.network.telnet;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.net.SocketFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.TelnetResult;
import pl.sgorski.nethelt.service.TelnetOperation;

/** Default implementation of TelnetOperation */
@Slf4j
@RequiredArgsConstructor
public class DefaultTelnetOperationImpl implements TelnetOperation {

  private static final int TELNET_TIMEOUT_MS = 5_000;

  private final SocketFactory socketFactory;

  public DefaultTelnetOperationImpl() {
    this(SocketFactory.getDefault());
  }

  @Override
  public TelnetResult execute(Device device) throws NetworkException {
    log.info("Checking port {} of device: {}", device.getPort(), device.getName());
    var startTime = System.nanoTime();
    var isPortOpen = checkIfPortIsOpen(device);
    var responseTime = getElapsedTimeInMs(startTime);
    var message = isPortOpen ?
      "Port " + device.getPort() + " is open in device " + device.getName() :
      "Port " + device.getPort() + " is closed in device " + device.getName();
    log.info("Telnet check for {} result: {}", device.getName(), message);
    return new TelnetResult(device, true, message, responseTime, isPortOpen);
  }

  /**
   * Check if the specified port on the device is open using {@link Socket} class
   *
   * @throws NetworkException when connection fails due to IO issues
   * @return true if the port is open, false otherwise
   */
  private boolean checkIfPortIsOpen(Device device) {
    if(Objects.isNull(device.getPort())) {
      throw new IllegalArgumentException("Port for device " + device.getName() + " is not specified. It is required for Telnet operation.");
    }

    try (Socket socket = socketFactory.createSocket()) {
      socket.connect(new InetSocketAddress(device.getAddress(), device.getPort()), TELNET_TIMEOUT_MS);
      return true;
    } catch (ConnectException | SocketTimeoutException | IllegalBlockingModeException e) {
      return false;
    } catch (IOException e) {
      throw new NetworkException("Telnet connection failed for device " + device.getName(), e);
    } catch (IllegalArgumentException e) {
      throw new NetworkException("Invalid port number for device " + device.getName() + ": " + device.getPort(), e);
    }
  }

  @Override
  public CompletableFuture<TelnetResult> executeAsync(Device device) throws NetworkException {
    return CompletableFuture.supplyAsync(() -> execute(device));
  }
}
