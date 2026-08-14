package pl.sgorski.nethelt.webapi.features.monitoring_task.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.PingTaskConfiguration;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestMonitoringTaskFactory;

public class MonitoringTaskTests {

  @Test
  void constructor_shouldCreateCorrectObject() {
    var device = TestDeviceFactory.createDevice();
    var configuration = new PingTaskConfiguration(Duration.ofSeconds(5));
    var task = new MonitoringTask(device, TaskType.PING, Duration.ofMinutes(5), configuration);

    assertSame(device, task.getDevice());
    assertSame(configuration, task.getConfiguration());
    assertEquals(TaskType.PING, task.getType());
    assertEquals(Duration.ofMinutes(5), task.getInterval());
    assertTrue(task.isEnabled());
  }

  @Test
  void update_shouldUpdateInterval() {
    var task = TestMonitoringTaskFactory.createTask(TaskType.PING, Duration.ofMinutes(5));

    task.update(Duration.ofSeconds(30));

    assertEquals(Duration.ofSeconds(30), task.getInterval());
  }

  @Test
  void enable_shouldEnableTask() {
    var task = TestMonitoringTaskFactory.createTask();

    task.disable();
    assertFalse(task.isEnabled());
    task.enable();

    assertTrue(task.isEnabled());
  }

  @Test
  void disable_shouldDisableTask() {
    var task = TestMonitoringTaskFactory.createTask();

    assertTrue(task.isEnabled());
    task.disable();

    assertFalse(task.isEnabled());
  }
}
