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
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskUpdateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.repository.MonitoringTaskRepository;

@Service
@RequiredArgsConstructor
public class MonitoringTaskService {

  private final MonitoringTaskRepository monitoringTaskRepository;
  private final DeviceService deviceService;

  public MonitoringTask getMonitoringTask(Long networkId, Long deviceId, Long monitoringTaskId) {
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

  @Transactional
  public MonitoringTask updateMonitoringTask(
      Long networkId, Long deviceId, Long monitoringTaskId, MonitoringTaskUpdateCommand command) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTask.update(Duration.ofSeconds(command.intervalSeconds()));
    return monitoringTask;
  }

  @Transactional
  public void enableMonitoringTask(Long networkId, Long deviceId, Long monitoringTaskId) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTask.enable();
  }

  @Transactional
  public void disableMonitoringTask(Long networkId, Long deviceId, Long monitoringTaskId) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTask.disable();
  }

  @Transactional
  public void deleteMonitoringTask(Long networkId, Long deviceId, Long monitoringTaskId) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTaskRepository.delete(monitoringTask);
  }
}
