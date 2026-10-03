package pl.sgorski.nethelt.agent.executor.service;

import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.Result;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
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

  public Set<PingResult> getPingResults(Set<MonitoringTask> tasks) {
    return execute(tasks, ping);
  }

  public Set<TelnetResult> getTelnetResults(Set<MonitoringTask> tasks) {
    return execute(tasks, telnet);
  }

  private <T extends Result> Set<T> execute(
      Set<MonitoringTask> tasks, NetworkOperation<T> operation) {
    return tasks.stream()
        .collect(
            Collectors.toMap(task -> task, task -> executor.submit(() -> operation.execute(task))))
        .entrySet()
        .stream()
        .map(entry -> getResult(entry.getKey(), entry.getValue(), operation))
        .collect(Collectors.toSet());
  }

  private <T extends Result> T getResult(
      MonitoringTask task, Future<T> future, NetworkOperation<T> operation) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Monitoring task interrupted.");
      return operation.error(task);
    } catch (ExecutionException e) {
      log.error("Monitoring task failed for {}", task.deviceId(), e.getCause());
      return operation.error(task);
    }
  }
}
