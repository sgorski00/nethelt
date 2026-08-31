package pl.sgorski.nethelt.agent.executor.handler;

import pl.sgorski.nethelt.agent.model.Operation;

public interface MonitoringTaskHandler {
  Operation getOperation();

  void execute();
}
