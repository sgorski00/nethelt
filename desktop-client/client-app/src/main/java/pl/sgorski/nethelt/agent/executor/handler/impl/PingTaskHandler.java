package pl.sgorski.nethelt.agent.executor.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.executor.service.MonitoringExecutor;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.webclient.api.web.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;

@Component
@RequiredArgsConstructor
public final class PingTaskHandler implements MonitoringTaskHandler {

  private final DeviceClient deviceClient;
  private final MonitoringResultClient monitoringResultClient;
  private final MonitoringExecutor monitoringExecutor;

  @Override
  public Operation getOperation() {
    return Operation.PING;
  }

  @Override
  public void execute() {
    var devices = deviceClient.getDevices();
    var results = monitoringExecutor.getPingResults(devices);
    monitoringResultClient.sendPingResults(results);
  }
}
