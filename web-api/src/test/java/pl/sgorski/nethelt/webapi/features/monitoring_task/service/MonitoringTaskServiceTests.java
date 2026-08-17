package pl.sgorski.nethelt.webapi.features.monitoring_task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.service.DeviceService;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskCreateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskUpdateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.HttpHealthcheckTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.PingTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.repository.MonitoringTaskRepository;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestMonitoringTaskFactory;

@ExtendWith(MockitoExtension.class)
public class MonitoringTaskServiceTests {

  @Mock private MonitoringTaskRepository monitoringTaskRepository;
  @Mock private MonitoringTaskConfigurationService monitoringTaskConfigurationService;
  @Mock private DeviceService deviceService;
  @InjectMocks private MonitoringTaskService monitoringTaskService;

  @Test
  void getMonitoringTask_shouldReturnMonitoringTask_whenMonitoringTaskExists() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTask =
        TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10));
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L))
        .thenReturn(Optional.of(monitoringTask));

    var result = monitoringTaskService.getMonitoringTask(1L, 1L, 1L);

    assertSame(monitoringTask, result);
  }

  @Test
  void getMonitoringTask_shouldThrowException_whenMonitoringTaskDoesNotExist() {
    var device = TestDeviceFactory.createDevice();
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L)).thenReturn(Optional.empty());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringTaskService.getMonitoringTask(1L, 1L, 1L));
  }

  @Test
  void getMonitoringTasks_shouldReturnMonitoringTasks() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTasks =
        Set.of(
            TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10)),
            TestMonitoringTaskFactory.createTask(device, TaskType.TELNET, Duration.ofSeconds(20)));
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findAllByDevice(device)).thenReturn(monitoringTasks);

    var result = monitoringTaskService.getMonitoringTasks(1L, 1L);

    assertIterableEquals(monitoringTasks, result);
  }

  @Test
  void createMonitoringTask_shouldCreateMonitoringTask_whenValidCommand() {
    var device = TestDeviceFactory.createDevice();
    var configuration = new HttpHealthcheckTaskConfigurationCommand(8080, "/health", 5000L);
    var command = new MonitoringTaskCreateCommand(TaskType.HTTP_HEALTHCHECK, 30L, configuration);
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskConfigurationService.createConfiguration(
            command.type(), command.configuration()))
        .thenReturn(TestMonitoringTaskFactory.createConfiguration(command.type()));
    when(monitoringTaskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = monitoringTaskService.createMonitoringTask(1L, 1L, command);

    assertEquals(device, result.getDevice());
    assertEquals(TaskType.HTTP_HEALTHCHECK, result.getType());
    assertEquals(Duration.ofSeconds(30L), result.getInterval());
    assertTrue(result.isEnabled());
    verify(monitoringTaskRepository).save(any());
  }

  @Test
  void createMonitoringTask_shouldThrow_whenConfigNotValid() {
    var device = TestDeviceFactory.createDevice();
    var configuration = new HttpHealthcheckTaskConfigurationCommand(8080, "/health", 5000L);
    var command = new MonitoringTaskCreateCommand(TaskType.HTTP_HEALTHCHECK, 30L, configuration);
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskConfigurationService.createConfiguration(
            command.type(), command.configuration()))
        .thenThrow(new MonitoringTaskValidationFailedException("Invalid configuration"));

    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> monitoringTaskService.createMonitoringTask(1L, 1L, command));
    verify(monitoringTaskRepository, never()).save(any());
  }

  @Test
  void updateMonitoringTask_shouldUpdateMonitoringTask_whenValidCommand() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTask =
        TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10));
    var configuration = new PingTaskConfigurationCommand(5000L);
    var command = new MonitoringTaskUpdateCommand(45L, configuration);
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskConfigurationService.createConfiguration(
            monitoringTask.getType(), command.configuration()))
        .thenReturn(TestMonitoringTaskFactory.createConfiguration(monitoringTask.getType()));
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L))
        .thenReturn(Optional.of(monitoringTask));

    var result = monitoringTaskService.updateMonitoringTask(1L, 1L, 1L, command);

    assertSame(monitoringTask, result);
    assertEquals(Duration.ofSeconds(45L), result.getInterval());
  }

  @Test
  void updateMonitoringTask_shouldThrow_whenConfigNotValid() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTask =
        TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10));
    var configuration = new PingTaskConfigurationCommand(-1L);
    var command = new MonitoringTaskUpdateCommand(45L, configuration);
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L))
        .thenReturn(Optional.of(monitoringTask));
    when(monitoringTaskConfigurationService.createConfiguration(
            monitoringTask.getType(), command.configuration()))
        .thenThrow(new MonitoringTaskValidationFailedException("Invalid configuration"));

    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> monitoringTaskService.updateMonitoringTask(1L, 1L, 1L, command));
  }

  @Test
  void updateMonitoringTask_shouldThrowException_whenMonitoringTaskDoesNotExist() {
    var device = TestDeviceFactory.createDevice();
    var configuration = new PingTaskConfigurationCommand(5000L);
    var command = new MonitoringTaskUpdateCommand(45L, configuration);
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L)).thenReturn(Optional.empty());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringTaskService.updateMonitoringTask(1L, 1L, 1L, command));
  }

  @Test
  void enableMonitoringTask_shouldEnableMonitoringTask() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTask =
        TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10));
    monitoringTask.disable();
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L))
        .thenReturn(Optional.of(monitoringTask));

    assertFalse(monitoringTask.isEnabled());
    var result = monitoringTaskService.enableMonitoringTask(1L, 1L, 1L);

    assertTrue(monitoringTask.isEnabled());
    assertSame(monitoringTask, result);
  }

  @Test
  void enableMonitoringTask_shouldThrowException_whenMonitoringTaskDoesNotExist() {
    var device = TestDeviceFactory.createDevice();
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L)).thenReturn(Optional.empty());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringTaskService.enableMonitoringTask(1L, 1L, 1L));
  }

  @Test
  void disableMonitoringTask_shouldDisableMonitoringTask() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTask =
        TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10));
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L))
        .thenReturn(Optional.of(monitoringTask));

    assertTrue(monitoringTask.isEnabled());
    var result = monitoringTaskService.disableMonitoringTask(1L, 1L, 1L);

    assertFalse(monitoringTask.isEnabled());
    assertSame(monitoringTask, result);
  }

  @Test
  void disableMonitoringTask_shouldThrowException_whenMonitoringTaskDoesNotExist() {
    var device = TestDeviceFactory.createDevice();
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L)).thenReturn(Optional.empty());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringTaskService.disableMonitoringTask(1L, 1L, 1L));
  }

  @Test
  void deleteMonitoringTask_shouldDeleteMonitoringTask() {
    var device = TestDeviceFactory.createDevice();
    var monitoringTask =
        TestMonitoringTaskFactory.createTask(device, TaskType.PING, Duration.ofSeconds(10));
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L))
        .thenReturn(Optional.of(monitoringTask));

    monitoringTaskService.deleteMonitoringTask(1L, 1L, 1L);

    verify(monitoringTaskRepository).delete(monitoringTask);
  }

  @Test
  void deleteMonitoringTask_shouldThrowException_whenMonitoringTaskDoesNotExist() {
    var device = TestDeviceFactory.createDevice();
    when(deviceService.getDevice(1L, 1L)).thenReturn(device);
    when(monitoringTaskRepository.findByDeviceAndId(device, 1L)).thenReturn(Optional.empty());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringTaskService.deleteMonitoringTask(1L, 1L, 1L));
    verify(monitoringTaskRepository, never()).delete(any());
  }
}
