package pl.sgorski.nethelt.agent.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.executor.handler.impl.PingTaskHandler;
import pl.sgorski.nethelt.agent.executor.handler.impl.TelnetTaskHandler;
import pl.sgorski.nethelt.agent.model.NetworkConfig;
import pl.sgorski.nethelt.agent.model.Operation;
import pl.sgorski.nethelt.agent.webclient.api.web.NetworkConfigClient;

@ExtendWith(MockitoExtension.class)
class MonitoringSchedulerTests {

  @Mock private NetworkConfigClient networkConfigClient;
  @Mock private ScheduledTaskManager monitoringTaskScheduler;
  @Mock private PingTaskHandler pingTaskHandler;
  @Mock private TelnetTaskHandler telnetTaskHandler;
  @Mock private ScheduledFuture scheduledFuture;

  private TaskUpdateScheduler scheduler;

  @BeforeEach
  void setUp() {
    this.scheduler =
        new TaskUpdateScheduler(
            networkConfigClient,
            monitoringTaskScheduler,
            List.of(pingTaskHandler, telnetTaskHandler));
  }

  @Test
  void updateTasks_ShouldSchedulePing_WhenEnabled() {
    mockHandlers(Operation.PING);
    var cfg = new NetworkConfig(Operation.PING, true, 10);
    mockConfig(cfg);
    mockScheduledTask();

    scheduler.updateTasks();

    verify(monitoringTaskScheduler).schedule(eq(10), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldNotSchedulePing_WhenDisabled() {
    var cfg = new NetworkConfig(Operation.PING, false, 10);
    mockConfig(cfg);

    scheduler.updateTasks();

    verify(monitoringTaskScheduler, never()).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldNotReschedulePing_WhenConfigurationUnchanged() {
    mockHandlers(Operation.PING);
    var cfg = new NetworkConfig(Operation.PING, true, 5);
    mockConfig(cfg);
    mockScheduledTask();

    scheduler.updateTasks();

    clearInvocations(monitoringTaskScheduler);

    scheduler.updateTasks();

    verify(monitoringTaskScheduler, never()).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldCancelPing_WhenDisabledAfterEnabled() {
    mockHandlers(Operation.PING);
    var enabledCfg = new NetworkConfig(Operation.PING, true, 5);
    var disabledCfg = new NetworkConfig(Operation.PING, false, 5);
    mockConfig(enabledCfg);
    mockScheduledTask();

    scheduler.updateTasks();
    verify(monitoringTaskScheduler).schedule(eq(5), any(Runnable.class));

    mockConfig(disabledCfg);
    scheduler.updateTasks();

    verify(monitoringTaskScheduler).cancel(any());
    verify(monitoringTaskScheduler, times(1)).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldScheduleTelnet_WhenEnabled() {
    mockHandlers(Operation.TELNET);
    var cfg = new NetworkConfig(Operation.TELNET, true, 5);
    mockConfig(cfg);
    mockScheduledTask();

    scheduler.updateTasks();

    verify(monitoringTaskScheduler).schedule(eq(5), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldNotScheduleTelnet_WhenDisabled() {
    var cfg = new NetworkConfig(Operation.TELNET, false, 5);
    mockConfig(cfg);

    scheduler.updateTasks();

    verify(monitoringTaskScheduler, never()).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldNotRescheduleTelnet_WhenConfigurationUnchanged() {
    mockHandlers(Operation.TELNET);
    var cfg = new NetworkConfig(Operation.TELNET, true, 5);
    mockConfig(cfg);
    mockScheduledTask();

    scheduler.updateTasks();
    clearInvocations(monitoringTaskScheduler);

    scheduler.updateTasks();
    verify(monitoringTaskScheduler, never()).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldCancelTelnet_WhenDisabledAfterEnabled() {
    mockHandlers(Operation.TELNET);
    var enabledCfg = new NetworkConfig(Operation.TELNET, true, 5);
    var disabledCfg = new NetworkConfig(Operation.TELNET, false, 5);
    mockConfig(enabledCfg);
    mockScheduledTask();

    scheduler.updateTasks();
    verify(monitoringTaskScheduler).schedule(eq(5), any(Runnable.class));

    mockConfig(disabledCfg);
    scheduler.updateTasks();

    verify(monitoringTaskScheduler).cancel(any());
    verify(monitoringTaskScheduler, times(1)).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldNotSchedule_WhenOperationIsNull() {
    var cfg = new NetworkConfig(null, true, 5);
    mockConfig(cfg);

    assertDoesNotThrow(scheduler::updateTasks);

    verify(monitoringTaskScheduler, never()).schedule(anyInt(), any(Runnable.class));
  }

  @Test
  void updateTasks_ShouldNotFail_WhenFetchingConfigurationThrowsException() {
    when(networkConfigClient.getNetworkConfigs())
        .thenThrow(new WebClientException("Server unavailable"));

    assertDoesNotThrow(scheduler::updateTasks);

    verify(monitoringTaskScheduler, never()).schedule(anyInt(), any(Runnable.class));
  }

  private void mockHandlers(Operation... operations) {
    var operationsList = Arrays.stream(operations).toList();
    if (operationsList.contains(Operation.PING)) {
      when(pingTaskHandler.getOperation()).thenReturn(Operation.PING);
    }
    if (operationsList.contains(Operation.TELNET)) {
      when(telnetTaskHandler.getOperation()).thenReturn(Operation.TELNET);
    }
  }

  private void mockScheduledTask() {
    when(monitoringTaskScheduler.schedule(anyInt(), any(Runnable.class)))
        .thenReturn(scheduledFuture);
  }

  private void mockConfig(NetworkConfig cfg) {
    when(networkConfigClient.getNetworkConfigs()).thenReturn(Set.of(cfg));
  }
}
