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
  private final MonitoringTaskConfigurationService monitoringTaskConfigurationService;
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
    var configuration =
        monitoringTaskConfigurationService.createConfiguration(
            command.type(), command.configuration());
    var interval = Duration.ofSeconds(command.intervalSeconds());
    var monitoringTask = new MonitoringTask(device, command.type(), interval, configuration);
    return monitoringTaskRepository.save(monitoringTask);
  }

  @Transactional
  public MonitoringTask updateMonitoringTask(
      Long networkId, Long deviceId, Long monitoringTaskId, MonitoringTaskUpdateCommand command) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    var interval = Duration.ofSeconds(command.intervalSeconds());
    var configuration =
        monitoringTaskConfigurationService.createConfiguration(
            monitoringTask.getType(), command.configuration());
    monitoringTask.update(interval, configuration);
    return monitoringTask;
  }

  @Transactional
  public MonitoringTask enableMonitoringTask(Long networkId, Long deviceId, Long monitoringTaskId) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTask.enable();
    return monitoringTask;
  }

  @Transactional
  public MonitoringTask disableMonitoringTask(
      Long networkId, Long deviceId, Long monitoringTaskId) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTask.disable();
    return monitoringTask;
  }

  @Transactional
  public void deleteMonitoringTask(Long networkId, Long deviceId, Long monitoringTaskId) {
    var monitoringTask = getMonitoringTask(networkId, deviceId, monitoringTaskId);
    monitoringTaskRepository.delete(monitoringTask);
  }
}
