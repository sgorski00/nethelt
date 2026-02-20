package pl.sgorski.nethelt.agent.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.executor.ResultProvider;
import pl.sgorski.nethelt.model.*;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebScheduledTaskManagerTests {

  private WebClientService webClientService;
  private ResultProvider resultProvider;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> scheduledFuture;
  private WebScheduledTaskManager manager;

  @BeforeEach
  void setUp() throws Exception {
    webClientService = mock(WebClientService.class);
    resultProvider = mock(ResultProvider.class);
    scheduler = mock(ScheduledExecutorService.class);
    scheduledFuture = mock(ScheduledFuture.class);

    manager = new WebScheduledTaskManager(webClientService, resultProvider);
    setPrivateField(manager, "scheduler", scheduler);
  }

  @Test
  void start_ShouldInvokeUpdateTasks() {
    manager.start();
    verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(), any(TimeUnit.class));
  }

  @Test
  void updateTasks_ShouldReschedulePing_Enabled() {
    var cfg = new NetworkConfig(Operation.PING, true, 10);
    var device = new Device();

    mockConfigAndDevices(cfg, device);
    when(resultProvider.getPingResults(any())).thenReturn(Collections.singleton(mock(PingResult.class)));
    doReturn(scheduledFuture).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

    invokeUpdateTasks();

    verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
  }

  @Test
  void updateTasks_ShouldNotReschedulePing_Disabled() {
    var cfg = new NetworkConfig(Operation.PING, false, 10);
    mockConfigAndDevices(cfg, new Device());
    invokeUpdateTasks();

    verify(scheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
  }

  @Test
  void updateTasks_ShouldNotReschedulePing_IntervalNotChanged() throws Exception {
    setPrivateField(manager, "pingInterval", 5);
    setPrivateField(manager, "pingEnabled", true);

    var cfg = new NetworkConfig(Operation.PING, true, 5);
    mockConfigAndDevices(cfg, new Device());
    invokeUpdateTasks();

    verify(scheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
  }

  @Test
  void updateTasks_ShouldCancelPing_WhenDisabledAfterEnabled() throws Exception {
    setPrivateField(manager, "pingEnabled", true);
    setPrivateField(manager, "pingTask", scheduledFuture);

    var cfg = new NetworkConfig(Operation.PING, false, 5);
    mockConfigAndDevices(cfg, new Device());
    invokeUpdateTasks();

    verify(scheduledFuture).cancel(false);
    verify(scheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
  }

  @Test
  void updateTasks_ShouldRescheduleTelnet_Enabled() {
    var cfg = new NetworkConfig(Operation.TELNET, true, 5);
    var device = new Device();
    device.setPort(22);

    mockConfigAndDevices(cfg, device);
    when(resultProvider.getTelnetResults(any())).thenReturn(Collections.singleton(mock(TelnetResult.class)));
    doReturn(scheduledFuture).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

    invokeUpdateTasks();

    verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(5L), eq(TimeUnit.SECONDS));
  }

  @Test
  void updateTasks_ShouldNotRescheduleTelnet_Disabled() {
    var cfg = new NetworkConfig(Operation.TELNET, false, 5);
    mockConfigAndDevices(cfg, new Device());
    invokeUpdateTasks();

    verify(scheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
  }

  @Test
  void updateTasks_ShouldNotRescheduleTelnet_IntervalNotChanged() throws Exception {
    setPrivateField(manager, "telnetInterval", 5);
    setPrivateField(manager, "telnetEnabled", true);

    var cfg = new NetworkConfig(Operation.TELNET, true, 5);
    var device = new Device();
    device.setPort(22);

    mockConfigAndDevices(cfg, device);
    invokeUpdateTasks();

    verify(scheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
  }

  @Test
  void updateTasks_ShouldNotReschedule_NullOperation() {
    var cfg = new NetworkConfig(null, true, 5);
    mockConfigAndDevices(cfg, new Device());
    invokeUpdateTasks();

    verify(scheduler, never()).scheduleWithFixedDelay(any(), anyLong(), anyLong(), any());
  }

  @Test
  void stop_ShouldCancelTasksAndShutdownScheduler() throws Exception {
    setPrivateField(manager, "pingTask", scheduledFuture);
    setPrivateField(manager, "telnetTask", scheduledFuture);

    manager.stop();

    verify(scheduledFuture, times(2)).cancel(false);
    verify(scheduler).shutdown();
  }

  private void mockConfigAndDevices(NetworkConfig cfg, Device device) {
    when(webClientService.fetchNetworkConfig()).thenReturn(Collections.singleton(cfg));
    when(webClientService.fetchDevices()).thenReturn(Collections.singleton(device));
  }

  private void invokeUpdateTasks() {
    try {
      var m = WebScheduledTaskManager.class.getDeclaredMethod("updateTasks");
      m.setAccessible(true);
      m.invoke(manager);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    var f = target.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    f.set(target, value);
  }
}
