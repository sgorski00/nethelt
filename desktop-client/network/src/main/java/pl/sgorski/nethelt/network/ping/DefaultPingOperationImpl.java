package pl.sgorski.nethelt.network.ping;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.service.PingOperation;

/** Default implementation of PingOperation */
@Slf4j
public final class DefaultPingOperationImpl implements PingOperation {

  private static final int PING_TIMEOUT_MS = 5_000;

  @Override
  public PingResult execute(Device device) throws NetworkException {
    log.info("Pinging device: {}", device.getName());
    var startTimes = System.nanoTime();
    try {
      var pingResult = device.getAddress().isReachable(PING_TIMEOUT_MS);
      var responseTime = getElapsedTimeInMs(startTimes);
      var message = pingResult ? "Ping successful" : "Timeout after " + responseTime + " ms";
      log.info("Pinging {} result: {}", device.getName(), message);
      return new PingResult(device, pingResult, message, responseTime);
    } catch (IOException e) {
      throw new NetworkException("Ping failed for device " + device.getName(), e);
    }
  }

  @Override
  public CompletableFuture<PingResult> executeAsync(Device device) throws NetworkException {
    return CompletableFuture.supplyAsync(() -> execute(device));
  }
}
