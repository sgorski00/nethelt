package pl.sgorski.nethelt.agent.scheduler.task;

import pl.sgorski.nethelt.agent.model.Operation;

public interface MonitoringTaskHandler {
  Operation getOperation();

  void execute();
}
