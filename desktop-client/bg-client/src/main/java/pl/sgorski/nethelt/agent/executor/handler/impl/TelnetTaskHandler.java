package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;

@Component
@RequiredArgsConstructor
public final class TelnetTaskHandler implements MonitoringTaskHandler {

  private final TelnetOperation telnetOperation;
  private final MonitoringResultClient monitoringResultClient;
  private final MonitoringResultMapper monitoringResultMapper;

  @Override
  public TaskType getOperation() {
    return TaskType.TELNET;
  }

  @Override
  public void execute(MonitoringTask task) {
    var result = telnetOperation.execute(task);
    var request = monitoringResultMapper.toRequest(result);
    monitoringResultClient.sendMonitoringResult(request);
  }
}
