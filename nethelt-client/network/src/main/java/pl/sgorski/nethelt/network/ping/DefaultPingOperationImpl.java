package pl.sgorski.nethelt.network.ping;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import pl.sgorski.nethelt.core.exception.NetworkException;
import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.PingResult;
import pl.sgorski.nethelt.core.service.PingOperation;

/** Default implementation of PingOperation */
public class DefaultPingOperationImpl implements PingOperation {

  private static final int PING_TIMEOUT_MS = 5_000;

  @Override
  public PingResult execute(Device device) throws NetworkException {
    long startTimes = System.nanoTime();
    try {
      boolean pingResult = device.getAddress().isReachable(PING_TIMEOUT_MS);
      long responseTime = getElapsedTimeInMs(startTimes);
      String message = pingResult ? "Ping successful" : "Timeout after " + responseTime + " ms";
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
