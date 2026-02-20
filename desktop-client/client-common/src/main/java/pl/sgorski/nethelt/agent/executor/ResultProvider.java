package pl.sgorski.nethelt.agent.executor;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;
import pl.sgorski.nethelt.service.PingOperation;
import pl.sgorski.nethelt.service.TelnetOperation;
import pl.sgorski.nethelt.network.ping.DefaultPingOperationImpl;
import pl.sgorski.nethelt.network.telnet.DefaultTelnetOperationImpl;

/**
 * Provides methods to execute network operations on a set of devices and retrieve their results.
 * This class utilizes asynchronous operations to perform network checks concurrently for better performance.
 */
@Slf4j
public final class ResultProvider {

  private final PingOperation ping;
  private final TelnetOperation telnet;

  public ResultProvider() {
    log.debug("Initializing ResultProvider with default implementations");
    this.ping = new DefaultPingOperationImpl();
    this.telnet = new DefaultTelnetOperationImpl();
  }

  /**
   * Constructor allowing dependency injection.
   * Should be used primarily for testing purposes.
   * It is recommended to use default constructor in production code.
   */
  public ResultProvider(PingOperation ping, TelnetOperation telnet) {
    log.warn("Using ResultProvider constructor with custom implementations. This is intended for testing purposes only.");
    this.ping = ping;
    this.telnet = telnet;
  }

  /**
   * Executes ping operations asynchronously on the provided set of devices and returns their results.
   *
   * @param devices the set of devices to ping
   * @return a set of PingResult objects containing the results of the ping operations
   */
  public Set<PingResult> getPingResults(Set<Device> devices) {
    var futures = devices.stream()
      .map(ping::executeAsync)
      .collect(Collectors.toSet());
    return waitForAll(futures);
  }

  /**
   * Executes telnet operations asynchronously on the provided set of devices and returns their results.
   * Devices without a specified port are filtered out before execution.
   *
   * @param devices the set of devices to perform telnet operations on
   * @return a set of TelnetResult objects containing the results of the telnet operations
   */
  public Set<TelnetResult> getTelnetResults(Set<Device> devices) {
    var futures = devices.stream()
      .filter(device -> Objects.nonNull(device.getPort()))
      .map(telnet::executeAsync)
      .collect(Collectors.toSet());
    return waitForAll(futures);
  }

  private static <T> Set<T> waitForAll(Set<CompletableFuture<T>> futures) {
    return futures.stream()
      .map(CompletableFuture::join)
      .collect(Collectors.toSet());
  }
}
