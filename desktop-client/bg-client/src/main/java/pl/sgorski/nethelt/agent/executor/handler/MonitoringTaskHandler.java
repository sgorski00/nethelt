package pl.sgorski.nethelt.agent.executor.handler;

import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;

public interface MonitoringTaskHandler {
  TaskType getOperation();

  void execute(MonitoringTask task);
}
