package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;

@Component
@RequiredArgsConstructor
public final class TelnetTaskHandler implements MonitoringTaskHandler {

  private final TelnetOperation telnetOperation;

  @Override
  public TaskType getOperation() {
    return TaskType.TELNET;
  }

  @Override
  public void execute(MonitoringTask task) {
    var result = telnetOperation.execute(task);
    // todo: result should be send to the server vai api
  }
}
