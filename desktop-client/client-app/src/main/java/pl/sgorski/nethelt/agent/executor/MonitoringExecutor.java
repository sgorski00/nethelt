package pl.sgorski.nethelt.agent.executor;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.model.TelnetResult;
import pl.sgorski.nethelt.agent.network.ping.PingOperation;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MonitoringExecutor {

  private final PingOperation ping;
  private final TelnetOperation telnet;
  private final ExecutorService executor;

  public Set<PingResult> getPingResults(Set<Device> devices) {
    var futures =
        devices.stream()
            .map(device -> executor.submit(() -> ping.execute(device)))
            .collect(Collectors.toSet());
    return collectResults(futures);
  }

  public Set<TelnetResult> getTelnetResults(Set<Device> devices) {
    var futures =
        devices.stream()
            .filter(device -> Objects.nonNull(device.getPort()))
            .map(device -> executor.submit(() -> telnet.execute(device)))
            .collect(Collectors.toSet());
    return collectResults(futures);
  }

  private <T> Set<T> collectResults(Set<Future<T>> futures) {
    return futures.stream()
        .map(this::getResult)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private <T> T getResult(Future<T> future) {
    // todo: return a result except of resturning null when an exception happens
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Monitoring task interrupted.");
      return null;
    } catch (ExecutionException e) {
      var cause = e.getCause();

      if (cause instanceof NetworkException) {
        log.warn("Network monitoring failed: {}", cause.getMessage());
      } else {
        log.error("Unexpected error during network monitoring.", cause);
      }

      return null;
    }
  }
}
