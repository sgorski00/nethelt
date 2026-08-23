package pl.sgorski.nethelt.agent.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import pl.sgorski.nethelt.agent.executor.MonitoringExecutor;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.NetworkConfig;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.test_utils.TestDeviceFactory;
import pl.sgorski.nethelt.agent.webclient.WebClientService;

@ExtendWith(MockitoExtension.class)
class WebScheduledTaskManagerTests {

  @Mock private WebClientService webClientService;
  @Mock private MonitoringExecutor monitoringExecutor;
  @Mock private TaskScheduler scheduler;
  @Mock private ScheduledFuture scheduledFuture;

  @InjectMocks private WebScheduledTaskManager manager;

  @Test
  void updateTasks_ShouldSchedulePing_WhenEnabled() {
    var cfg = new NetworkConfig(Operation.PING, true, 10);

    mockConfigAndDevices(cfg, TestDeviceFactory.createDeviceWithoutPort());
    mockScheduledTask();

    manager.updateTasks();

    verify(scheduler)
        .scheduleWithFixedDelay(
            any(Runnable.class), any(Instant.class), eq(Duration.ofSeconds(10)));
  }

  @Test
  void updateTasks_ShouldNotSchedulePing_WhenDisabled() {
    var cfg = new NetworkConfig(Operation.PING, false, 10);

    mockConfigAndDevices(cfg, TestDeviceFactory.createDeviceWithoutPort());

    manager.updateTasks();

    verify(scheduler, never())
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldNotReschedulePing_WhenConfigurationUnchanged() {
    var cfg = new NetworkConfig(Operation.PING, true, 5);

    mockConfigAndDevices(cfg, TestDeviceFactory.createDeviceWithoutPort());
    mockScheduledTask();

    manager.updateTasks();

    clearInvocations(scheduler);

    manager.updateTasks();

    verify(scheduler, never())
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldCancelPing_WhenDisabledAfterEnabled() {
    var enabledCfg = new NetworkConfig(Operation.PING, true, 5);
    var disabledCfg = new NetworkConfig(Operation.PING, false, 5);

    mockConfigAndDevices(enabledCfg, TestDeviceFactory.createDeviceWithoutPort());
    mockScheduledTask();

    manager.updateTasks();

    verify(scheduler)
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), eq(Duration.ofSeconds(5)));

    mockConfigAndDevices(disabledCfg, TestDeviceFactory.createDeviceWithoutPort());

    manager.updateTasks();

    verify(scheduledFuture).cancel(false);

    verify(scheduler, times(1))
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldScheduleTelnet_WhenEnabled() {
    var cfg = new NetworkConfig(Operation.TELNET, true, 5);
    var device = TestDeviceFactory.createDeviceWithPort(22);

    mockConfigAndDevices(cfg, device);
    mockScheduledTask();

    manager.updateTasks();

    verify(scheduler)
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), eq(Duration.ofSeconds(5)));
  }

  @Test
  void updateTasks_ShouldNotScheduleTelnet_WhenDisabled() {
    var cfg = new NetworkConfig(Operation.TELNET, false, 5);

    mockConfigAndDevices(cfg, TestDeviceFactory.createDeviceWithPort());

    manager.updateTasks();

    verify(scheduler, never())
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldNotRescheduleTelnet_WhenConfigurationUnchanged() {
    var cfg = new NetworkConfig(Operation.TELNET, true, 5);
    var device = TestDeviceFactory.createDeviceWithPort(22);

    mockConfigAndDevices(cfg, device);
    mockScheduledTask();

    manager.updateTasks();

    clearInvocations(scheduler);

    manager.updateTasks();

    verify(scheduler, never())
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldCancelTelnet_WhenDisabledAfterEnabled() {
    var enabledCfg = new NetworkConfig(Operation.TELNET, true, 5);
    var disabledCfg = new NetworkConfig(Operation.TELNET, false, 5);

    mockConfigAndDevices(enabledCfg, TestDeviceFactory.createDeviceWithPort());
    mockScheduledTask();

    manager.updateTasks();

    verify(scheduler)
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), eq(Duration.ofSeconds(5)));

    mockConfigAndDevices(disabledCfg, TestDeviceFactory.createDeviceWithPort());

    manager.updateTasks();

    verify(scheduledFuture).cancel(false);

    verify(scheduler, times(1))
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldNotSchedule_WhenOperationIsNull() {
    var cfg = new NetworkConfig(null, true, 5);

    mockConfigAndDevices(cfg, TestDeviceFactory.createDeviceWithPort());

    assertDoesNotThrow(manager::updateTasks);

    verify(scheduler, never())
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldNotFail_WhenFetchingConfigurationThrowsException() {
    when(webClientService.fetchNetworkConfig())
        .thenThrow(new RuntimeException("Server unavailable"));

    assertDoesNotThrow(manager::updateTasks);

    verify(webClientService, never()).fetchDevices();
    verify(scheduler, never())
        .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
  }

  @Test
  void updateTasks_ShouldNotFetchDevices_WhenConfigurationFetchFails() {
    when(webClientService.fetchNetworkConfig())
        .thenThrow(new RuntimeException("Server unavailable"));

    manager.updateTasks();

    verify(webClientService, never()).fetchDevices();
  }

  private void mockScheduledTask() {
    when(scheduler.scheduleWithFixedDelay(
            any(Runnable.class), any(Instant.class), any(Duration.class)))
        .thenReturn(scheduledFuture);
  }

  private void mockConfigAndDevices(NetworkConfig cfg, Device device) {
    when(webClientService.fetchNetworkConfig()).thenReturn(Set.of(cfg));
    when(webClientService.fetchDevices()).thenReturn(Set.of(device));
  }
}
