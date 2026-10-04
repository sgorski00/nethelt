package pl.sgorski.nethelt.agent.network.ping.impl;

import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.PingTaskConfiguration;
import pl.sgorski.nethelt.agent.network.ping.PingOperation;

@Slf4j
@Component
public final class DefaultPingOperation implements PingOperation {

  @Override
  public PingResult execute(MonitoringTask task) throws NetworkException {
    log.info("Pinging device: {}", task.deviceId());
    var startTime = System.nanoTime();
    try {
      var configuration = (PingTaskConfiguration) task.configuration();
      var timeoutMs = (int) configuration.timeout().toMillis();
      var pingResult = task.deviceIp().isReachable(timeoutMs);
      var responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
      var message = pingResult ? "Ping successful" : "Timeout after " + responseTime + " ms";
      log.info("Pinging {} result: {}", task.deviceId(), message);
      return new PingResult(task.id(), pingResult, message, responseTime);
    } catch (IOException e) {
      throw new NetworkException("Ping failed for device " + task.deviceId(), e);
    }
  }

  @Override
  public PingResult error(MonitoringTask task) {
    return new PingResult(task.id(), false, "Ping failed", null);
  }
}
