package pl.sgorski.nethelt.agent.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTaskKey;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.scheduler.ScheduledTaskManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringTaskService {

  private final ScheduledTaskManager scheduledTaskManager;
  private final List<MonitoringTaskHandler> handlers;

  private final Map<MonitoringTaskKey, MonitoringTask> currentTasks = new HashMap<>();
  private final Map<MonitoringTaskKey, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

  public void synchronize(List<MonitoringTask> tasks) {
    var newTasks = toTaskMap(tasks);

    removeDeletedTasks(newTasks);
    updateTasks(newTasks);

    currentTasks.clear();
    currentTasks.putAll(newTasks);
  }

  private Map<MonitoringTaskKey, MonitoringTask> toTaskMap(List<MonitoringTask> tasks) {
    return tasks.stream()
        .collect(
            Collectors.toMap(
                task -> new MonitoringTaskKey(task.deviceId(), task.id()), Function.identity()));
  }

  private void removeDeletedTasks(Map<MonitoringTaskKey, MonitoringTask> newTasks) {
    currentTasks.keySet().stream()
        .filter(key -> !newTasks.containsKey(key))
        .toList()
        .forEach(
            key -> {
              cancelTask(key);
              log.info("Monitoring task {} was removed", key);
            });
  }

  private void updateTasks(Map<MonitoringTaskKey, MonitoringTask> newTasks) {
    for (var entry : newTasks.entrySet()) {
      var key = entry.getKey();
      var newTask = entry.getValue();
      var currentTask = currentTasks.get(key);

      if (currentTask == null) {
        addTask(key, newTask);
        continue;
      }

      if (isUnchanged(currentTask, newTask)) {
        continue;
      }

      updateTask(key, newTask);
    }
  }

  private boolean isUnchanged(MonitoringTask current, MonitoringTask incoming) {
    return current.updatedAt().equals(incoming.updatedAt());
  }

  private void updateTask(MonitoringTaskKey key, MonitoringTask task) {
    cancelTask(key);
    addTask(key, task);
  }

  private void addTask(MonitoringTaskKey key, MonitoringTask task) {
    currentTasks.put(key, task);
    if (!task.enabled()) {
      log.info("Monitoring task {} is disabled", key);
      return;
    }
    scheduleTask(key, task);
  }

  private void cancelTask(MonitoringTaskKey key) {
    var scheduledTask = scheduledTasks.remove(key);

    if (scheduledTask != null) {
      scheduledTaskManager.cancel(scheduledTask);
    }

    currentTasks.remove(key);
  }

  private void scheduleTask(MonitoringTaskKey key, MonitoringTask task) {
    var handler = getHandler(task.type());
    var scheduledTask =
        scheduledTaskManager.schedule(task.interval().toSeconds(), () -> handler.execute(task));

    scheduledTasks.put(key, scheduledTask);
    log.info("Monitoring task {} scheduled with interval {}", key, task.interval());
  }

  private MonitoringTaskHandler getHandler(TaskType type) {
    return handlers.stream()
        .filter(h -> h.getOperation() == type)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Handler not found: " + type));
  }
}
