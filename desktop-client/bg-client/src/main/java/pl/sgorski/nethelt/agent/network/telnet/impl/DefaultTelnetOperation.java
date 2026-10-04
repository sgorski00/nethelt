package pl.sgorski.nethelt.agent.network.telnet.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.time.Duration;
import javax.net.SocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TelnetTaskConfiguration;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;

@Slf4j
@Component
public final class DefaultTelnetOperation implements TelnetOperation {

  private final SocketFactory socketFactory;

  private TelnetTaskConfiguration configuration;

  public DefaultTelnetOperation() {
    this.socketFactory = SocketFactory.getDefault();
  }

  protected DefaultTelnetOperation(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  @Override
  public TelnetResult execute(MonitoringTask task) throws NetworkException {
    configuration = (TelnetTaskConfiguration) task.configuration();
    log.info("Checking port {} of device with id: {}", configuration.port(), task.deviceId());
    var startTime = System.nanoTime();
    var isPortOpen = checkIfPortIsOpen(task);
    var responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
    var message =
        isPortOpen
            ? "Port " + configuration.port() + " is open in device " + task.deviceId()
            : "Port " + configuration.port() + " is closed in device " + task.deviceId();
    log.info("Telnet check for device with id: {},result: {}", task.deviceId(), message);
    return new TelnetResult(task.id(), true, message, responseTime, isPortOpen);
  }

  private boolean checkIfPortIsOpen(MonitoringTask task) {
    try (var socket = socketFactory.createSocket()) {
      socket.connect(
          new InetSocketAddress(task.deviceIp(), configuration.port()),
          (int) configuration.timeout().toMillis());
      return true;
    } catch (ConnectException | SocketTimeoutException | IllegalBlockingModeException e) {
      return false;
    } catch (IOException e) {
      throw new NetworkException("Telnet connection failed for device " + task.deviceId(), e);
    } catch (IllegalArgumentException e) {
      throw new NetworkException(
          "Invalid port number for device " + task.deviceId() + ": " + configuration.port(), e);
    }
  }

  @Override
  public TelnetResult error(MonitoringTask task) {
    return new TelnetResult(task.id(), false, "Telnet check failed", null, false);
  }
}
