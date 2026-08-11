package pl.sgorski.nethelt.webapi.features.monitoring_task.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;

public class MonitoringTaskTests {

  @Test
  void constructor_shouldCreateCorrectObject() {
    var device = TestDeviceFactory.createDevice();
    var task = new MonitoringTask(device, TaskType.PING, Duration.ofMinutes(5));

    assertSame(device, task.getDevice());
    assertEquals(TaskType.PING, task.getType());
    assertEquals(Duration.ofMinutes(5), task.getInterval());
    assertTrue(task.isEnabled());
  }

  @Test
  void enable_shouldEnableDevice() {
    var device = TestDeviceFactory.createDevice();
    var task = new MonitoringTask(device, TaskType.PING, Duration.ofMinutes(5));

    task.disable();
    assertFalse(task.isEnabled());
    task.enable();

    assertTrue(task.isEnabled());
  }

  @Test
  void disable_shouldDisableDevice() {
    var device = TestDeviceFactory.createDevice();
    var task = new MonitoringTask(device, TaskType.PING, Duration.ofMinutes(5));

    assertTrue(task.isEnabled());
    task.disable();

    assertFalse(task.isEnabled());
  }
}
