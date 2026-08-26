package pl.sgorski.nethelt.agent.scheduler.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.MonitoringExecutor;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.webclient.service.WebClientService;

@Component
@RequiredArgsConstructor
public final class PingTaskHandler implements MonitoringTaskHandler {

  private final WebClientService webClientService;
  private final MonitoringExecutor monitoringExecutor;

  @Override
  public Operation getOperation() {
    return Operation.PING;
  }

  @Override
  public void execute() {
    var devices = webClientService.fetchDevices();
    var results = monitoringExecutor.getPingResults(devices);
    webClientService.sendPingResults(results);
  }
}
