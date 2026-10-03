package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.http_healthcheck.HttpHealthcheckOperation;

@Component
@RequiredArgsConstructor
public final class HttpHealthcheckTaskHandler implements MonitoringTaskHandler {

  private final HttpHealthcheckOperation httpHealthcheckOperation;

  @Override
  public TaskType getOperation() {
    return TaskType.HTTP_HEALTHCHECK;
  }

  @Override
  public void execute(MonitoringTask task) {
    var result = httpHealthcheckOperation.execute(task);
    // todo: result should be send to the server vai api
  }
}
