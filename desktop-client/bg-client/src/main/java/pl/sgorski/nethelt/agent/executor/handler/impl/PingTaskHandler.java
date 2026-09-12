package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.executor.service.MonitoringExecutor;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.webclient.api.web.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;

@Component
@RequiredArgsConstructor
public final class PingTaskHandler implements MonitoringTaskHandler {

  private final DeviceClient deviceClient;
  private final MonitoringResultClient monitoringResultClient;
  private final MonitoringExecutor monitoringExecutor;

  @Override
  public TaskType getOperation() {
    return TaskType.PING;
  }

  @Override
  public void execute(MonitoringTask task) {
    // todo: implement
    var devices = deviceClient.getDevices();
    var results = monitoringExecutor.getPingResults(devices);
    monitoringResultClient.sendPingResults(results);
  }
}
