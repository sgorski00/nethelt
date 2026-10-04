package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.http_healthcheck.HttpHealthcheckOperation;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;

@Component
@RequiredArgsConstructor
public final class HttpHealthcheckTaskHandler implements MonitoringTaskHandler {

  private final HttpHealthcheckOperation httpHealthcheckOperation;
  private final MonitoringResultClient monitoringResultClient;
  private final MonitoringResultMapper monitoringResultMapper;

  @Override
  public TaskType getOperation() {
    return TaskType.HTTP_HEALTHCHECK;
  }

  @Override
  public void execute(MonitoringTask task) {
    var result = httpHealthcheckOperation.execute(task);
    var request = monitoringResultMapper.toRequest(result);
    monitoringResultClient.sendMonitoringResult(request);
  }
}
