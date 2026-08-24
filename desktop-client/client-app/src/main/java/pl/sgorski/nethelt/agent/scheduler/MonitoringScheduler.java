package pl.sgorski.nethelt.agent.scheduler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.model.NetworkConfig;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.scheduler.task.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.webclient.WebClientService;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MonitoringScheduler {

  private static final int CONFIG_UPDATE_INTERVAL_SEC = 60;

  private final WebClientService webClientService;
  private final MonitoringTaskScheduler scheduler;
  private final List<MonitoringTaskHandler> handlers;

  private final Map<Operation, ScheduledFuture<?>> scheduledTasks = new EnumMap<>(Operation.class);

  // todo: replace networkconfig with monitoring tasks
  private final Map<Operation, NetworkConfig> currentConfigs = new EnumMap<>(Operation.class);

  @Scheduled(fixedDelay = CONFIG_UPDATE_INTERVAL_SEC, timeUnit = TimeUnit.SECONDS)
  void updateTasks() {
    try {
      var configs = webClientService.fetchNetworkConfig();
      for (var config : configs) {
        updateTask(config);
      }
    } catch (Exception e) {
      log.error("Error while updating tasks: {}", e.getMessage(), e);
    }
  }

  private void updateTask(NetworkConfig config) {
    var operation = config.getOperation();
    var previous = currentConfigs.get(operation);
    if (previous != null
        && !config.isChanged(previous.isEnabled(), previous.getIntervalSeconds())) {
      return;
    }

    currentConfigs.put(operation, config);
    cancel(operation);
    if (!config.isEnabled()) {
      log.info("{} monitoring is disabled", operation);
      return;
    }
    var handler = getHandler(operation);
    var task = scheduler.schedule(config.getIntervalSeconds(), handler::execute);
    scheduledTasks.put(operation, task);
    log.info(
        "{} monitoring task scheduled with interval {} seconds",
        operation,
        config.getIntervalSeconds());
  }

  private MonitoringTaskHandler getHandler(Operation operation) {
    return handlers.stream()
        .filter(h -> h.getOperation() == operation)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Handler not found: " + operation));
  }

  private void cancel(Operation operation) {
    var task = scheduledTasks.remove(operation);
    if (task != null) {
      scheduler.close(task);
    }
  }
}
