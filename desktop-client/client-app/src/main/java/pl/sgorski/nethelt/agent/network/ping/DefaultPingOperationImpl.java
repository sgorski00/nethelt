package pl.sgorski.nethelt.agent.network.ping;

import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.service.PingOperation;

@Slf4j
@Component
public class DefaultPingOperationImpl implements PingOperation {

  private static final int PING_TIMEOUT_MS = 5_000;

  @Override
  public PingResult execute(Device device) throws NetworkException {
    log.info("Pinging device: {}", device.getName());
    var startTime = System.nanoTime();
    try {
      var pingResult = device.getAddress().isReachable(PING_TIMEOUT_MS);
      var responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
      var message = pingResult ? "Ping successful" : "Timeout after " + responseTime + " ms";
      log.info("Pinging {} result: {}", device.getName(), message);
      return new PingResult(device, pingResult, message, responseTime);
    } catch (IOException e) {
      throw new NetworkException("Ping failed for device " + device.getName(), e);
    }
  }
}
