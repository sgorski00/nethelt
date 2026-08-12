package pl.sgorski.nethelt.webapi.utils;

import java.time.Duration;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public final class TestMonitoringTaskFactory {

  public static MonitoringTask createTask() {
    var device = TestDeviceFactory.createDevice();
    return createTask(device, TaskType.PING, Duration.ofSeconds(10));
  }

  public static MonitoringTask createTask(TaskType type, Duration interval) {
    var device = TestDeviceFactory.createDevice();
    return createTask(device, type, interval);
  }

  public static MonitoringTask createTask(Device device, TaskType type, Duration interval) {
    return new MonitoringTask(device, type, interval);
  }
}
