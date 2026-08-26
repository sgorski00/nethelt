package pl.sgorski.nethelt.agent.scheduler.task;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.MonitoringExecutor;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.webclient.service.WebClientService;

@Component
@RequiredArgsConstructor
public final class TelnetTaskHandler implements MonitoringTaskHandler {

  private final WebClientService webClientService;
  private final MonitoringExecutor monitoringExecutor;

  @Override
  public Operation getOperation() {
    return Operation.TELNET;
  }

  @Override
  public void execute() {
    var devices =
        webClientService.fetchDevices().stream()
            .filter(device -> device.getPort() != null)
            .collect(Collectors.toSet());
    var results = monitoringExecutor.getTelnetResults(devices);
    webClientService.sendTelnetResults(results);
  }
}
