package pl.sgorski.nethelt.agent.executor;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.dto.MonitoringTask;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.model.Result;
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
    var tasks =
        devices.stream()
            .map(
                device -> new MonitoringTask<>(device, executor.submit(() -> ping.execute(device))))
            .collect(Collectors.toSet());
    return collectResults(tasks, ping::error);
  }

  public Set<TelnetResult> getTelnetResults(Set<Device> devices) {
    var tasks =
        devices.stream()
            .filter(device -> Objects.nonNull(device.getPort()))
            .map(
                device ->
                    new MonitoringTask<>(device, executor.submit(() -> telnet.execute(device))))
            .collect(Collectors.toSet());
    return collectResults(tasks, telnet::error);
  }

  private <T extends Result> Set<T> collectResults(
      Set<MonitoringTask<T>> tasks, Function<Device, T> errorResultFactory) {
    return tasks.stream()
        .map(task -> getResult(task, errorResultFactory))
        .collect(Collectors.toSet());
  }

  private <T extends Result> T getResult(
      MonitoringTask<T> task, Function<Device, T> errorResultFactory) {
    try {
      return task.future().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Monitoring task interrupted.");
      return errorResultFactory.apply(task.device());
    } catch (ExecutionException e) {
      log.error("Monitoring task failed for {}", task.device().getName(), e.getCause());
      return errorResultFactory.apply(task.device());
    }
  }
}
