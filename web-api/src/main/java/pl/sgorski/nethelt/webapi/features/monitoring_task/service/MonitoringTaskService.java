package pl.sgorski.nethelt.webapi.features.monitoring_task.service;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskNotFoundException;
import pl.sgorski.nethelt.webapi.features.device.service.DeviceService;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskCreateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.repository.MonitoringTaskRepository;

@Service
@RequiredArgsConstructor
public class MonitoringTaskService {

  private final MonitoringTaskRepository monitoringTaskRepository;
  private final DeviceService deviceService;

  public MonitoringTask findByIdAndDevice(Long networkId, Long deviceId, Long monitoringTaskId) {
    var device = deviceService.getDevice(networkId, deviceId);
    return monitoringTaskRepository
        .findByDeviceAndId(device, monitoringTaskId)
        .orElseThrow(MonitoringTaskNotFoundException::new);
  }

  public Set<MonitoringTask> getMonitoringTasks(Long networkId, Long deviceId) {
    var device = deviceService.getDevice(networkId, deviceId);
    return monitoringTaskRepository.findAllByDevice(device);
  }

  @Transactional
  public MonitoringTask createMonitoringTask(
      Long networkId, Long deviceId, MonitoringTaskCreateCommand command) {
    var device = deviceService.getDevice(networkId, deviceId);
    var monitoringTask =
        new MonitoringTask(device, command.type(), Duration.ofSeconds(command.intervalSeconds()));
    return monitoringTaskRepository.save(monitoringTask);
  }
}
