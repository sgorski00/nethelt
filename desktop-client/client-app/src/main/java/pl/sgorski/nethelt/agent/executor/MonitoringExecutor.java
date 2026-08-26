package pl.sgorski.nethelt.agent.executor;

import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.model.Result;
import pl.sgorski.nethelt.agent.model.TelnetResult;
import pl.sgorski.nethelt.agent.network.NetworkOperation;
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
    return execute(devices, ping);
  }

  public Set<TelnetResult> getTelnetResults(Set<Device> devices) {
    return execute(devices, telnet);
  }

  private <T extends Result> Set<T> execute(Set<Device> devices, NetworkOperation<T> operation) {
    return devices.stream()
        .collect(
            Collectors.toMap(
                device -> device, device -> executor.submit(() -> operation.execute(device))))
        .entrySet()
        .stream()
        .map(entry -> getResult(entry.getKey(), entry.getValue(), operation))
        .collect(Collectors.toSet());
  }

  private <T extends Result> T getResult(
      Device device, Future<T> future, NetworkOperation<T> operation) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Monitoring task interrupted.");
      return operation.error(device);
    } catch (ExecutionException e) {
      log.error("Monitoring task failed for {}", device.getName(), e.getCause());
      return operation.error(device);
    }
  }
}
