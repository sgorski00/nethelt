package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.ping.PingOperation;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PingTaskHandler implements MonitoringTaskHandler {

  private final PingOperation pingOperation;

  @Override
  public TaskType getOperation() {
    return TaskType.PING;
  }

  @Override
  public void execute(MonitoringTask task) {
    var result = pingOperation.execute(task);
    log.info("[Placeholder] Ping result: {}", result);
    // todo: result should be send to the server vai api
  }
}
