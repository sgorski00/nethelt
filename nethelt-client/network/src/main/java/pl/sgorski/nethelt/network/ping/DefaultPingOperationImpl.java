package pl.sgorski.nethelt.network.ping;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.service.PingOperation;

/** Default implementation of PingOperation */
public class DefaultPingOperationImpl implements PingOperation {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPingOperationImpl.class);
  private static final int PING_TIMEOUT_MS = 5_000;

  @Override
  public PingResult execute(Device device) throws NetworkException {
    LOG.info("Pinging device: {}", device.getName());
    long startTimes = System.nanoTime();
    try {
      boolean pingResult = device.getAddress().isReachable(PING_TIMEOUT_MS);
      long responseTime = getElapsedTimeInMs(startTimes);
      String message = pingResult ? "Ping successful" : "Timeout after " + responseTime + " ms";
      LOG.info("Pinging {} result: {}", device.getName(), message);
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
