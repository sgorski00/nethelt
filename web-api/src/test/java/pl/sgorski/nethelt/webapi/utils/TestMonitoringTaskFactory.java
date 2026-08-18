package pl.sgorski.nethelt.webapi.utils;

import java.time.Duration;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.PingTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.TelnetTaskConfiguration;

public final class TestMonitoringTaskFactory {

  public static MonitoringTask createTask() {
    var device = TestDeviceFactory.createDevice();
    var configuration = new PingTaskConfiguration(Duration.ofSeconds(5));
    return createTask(device, TaskType.PING, Duration.ofSeconds(10), configuration);
  }

  public static MonitoringTask createTask(TaskType type, Duration interval) {
    var device = TestDeviceFactory.createDevice();
    var configuration = createConfiguration(type);
    return createTask(device, type, interval, configuration);
  }

  public static MonitoringTask createTask(Device device, TaskType type, Duration interval) {
    var configuration = createConfiguration(type);
    return new MonitoringTask(device, type, interval, configuration);
  }

  public static MonitoringTask createTask(
      Device device, TaskType type, Duration interval, MonitoringTaskConfiguration configuration) {
    return new MonitoringTask(device, type, interval, configuration);
  }

  public static MonitoringTaskConfiguration createConfiguration(TaskType type) {
    return switch (type) {
      case PING -> new PingTaskConfiguration(Duration.ofSeconds(2));
      case TELNET -> new TelnetTaskConfiguration(8080, Duration.ofSeconds(2));
      case HTTP_HEALTHCHECK ->
          new HttpHealthcheckTaskConfiguration(8080, "/healthcheck", Duration.ofSeconds(3));
    };
  }
}
