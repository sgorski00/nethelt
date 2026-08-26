package pl.sgorski.nethelt.agent.scheduler.task;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.MonitoringExecutor;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.webclient.api.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.MonitoringResultClient;

@Component
@RequiredArgsConstructor
public final class TelnetTaskHandler implements MonitoringTaskHandler {

  private final DeviceClient deviceClient;
  private final MonitoringResultClient monitoringResultClient;
  private final MonitoringExecutor monitoringExecutor;

  @Override
  public Operation getOperation() {
    return Operation.TELNET;
  }

  @Override
  public void execute() {
    var devices =
        deviceClient.getDevices().stream()
            .filter(device -> device.getPort() != null)
            .collect(Collectors.toSet());
    var results = monitoringExecutor.getTelnetResults(devices);
    monitoringResultClient.sendTelnetResults(results);
  }
}
