package pl.sgorski.nethelt.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.executor.handler.MonitoringTaskHandler;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.scheduler.ScheduledTaskManager;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;

@ExtendWith(MockitoExtension.class)
public class MonitoringTaskServiceTests {

  @Mock private ScheduledTaskManager scheduledTaskManager;
  @Mock private MonitoringTaskHandler pingHandler;
  @Mock private MonitoringTaskHandler telnetHandler;
  @Mock private ScheduledFuture<?> scheduledFuture;

  private MonitoringTaskService monitoringTaskService;

  @BeforeEach
  void setUp() {
    lenient().when(pingHandler.getOperation()).thenReturn(TaskType.PING);
    lenient().when(telnetHandler.getOperation()).thenReturn(TaskType.TELNET);
    lenient().doReturn(scheduledFuture).when(scheduledTaskManager).schedule(anyLong(), any());
    monitoringTaskService =
        new MonitoringTaskService(scheduledTaskManager, List.of(pingHandler, telnetHandler));
  }

  @Test
  void synchronize_shouldScheduleNewEnabledTask() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);

    monitoringTaskService.synchronize(List.of(task));

    verify(scheduledTaskManager).schedule(eq(30L), any());
  }

  @Test
  void synchronize_shouldExecuteMatchingHandler_whenScheduledTaskRuns() {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    monitoringTaskService.synchronize(List.of(task));
    verify(scheduledTaskManager).schedule(anyLong(), runnableCaptor.capture());
    runnableCaptor.getValue().run();

    verify(telnetHandler).execute(task);
    verify(pingHandler, never()).execute(any());
  }

  @Test
  void synchronize_shouldNotScheduleDisabledTask() {
    var task = disabled(TestMonitoringTaskFactory.createPingMonitoringTask(1L));

    monitoringTaskService.synchronize(List.of(task));

    verifyNoInteractions(scheduledTaskManager);
  }

  @Test
  void synchronize_shouldNotRescheduleUnchangedTask() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);

    monitoringTaskService.synchronize(List.of(task));
    monitoringTaskService.synchronize(List.of(task));

    verify(scheduledTaskManager, times(1)).schedule(anyLong(), any());
    verify(scheduledTaskManager, never()).cancel(any());
  }

  @Test
  void synchronize_shouldRescheduleTask_whenTaskWasUpdated() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var updatedTask = withUpdatedAt(task, task.updatedAt().plusSeconds(1));

    monitoringTaskService.synchronize(List.of(task));
    monitoringTaskService.synchronize(List.of(updatedTask));

    verify(scheduledTaskManager).cancel(scheduledFuture);
    verify(scheduledTaskManager, times(2)).schedule(anyLong(), any());
  }

  @Test
  void synchronize_shouldCancelTask_whenTaskWasDisabled() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var disabledTask = disabled(withUpdatedAt(task, task.updatedAt().plusSeconds(1)));

    monitoringTaskService.synchronize(List.of(task));
    monitoringTaskService.synchronize(List.of(disabledTask));

    verify(scheduledTaskManager).cancel(scheduledFuture);
    verify(scheduledTaskManager, times(1)).schedule(anyLong(), any());
  }

  @Test
  void synchronize_shouldCancelTask_whenTaskWasRemoved() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);

    monitoringTaskService.synchronize(List.of(task));
    monitoringTaskService.synchronize(List.of());

    verify(scheduledTaskManager).cancel(scheduledFuture);
  }

  @Test
  void synchronize_shouldScheduleTaskAgain_whenRemovedTaskReturns() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);

    monitoringTaskService.synchronize(List.of(task));
    monitoringTaskService.synchronize(List.of());
    monitoringTaskService.synchronize(List.of(task));

    verify(scheduledTaskManager, times(2)).schedule(anyLong(), any());
  }

  @Test
  void synchronize_shouldNotCancelAnything_whenRemovedTaskWasDisabled() {
    var task = disabled(TestMonitoringTaskFactory.createPingMonitoringTask(1L));

    monitoringTaskService.synchronize(List.of(task));
    monitoringTaskService.synchronize(List.of());

    verifyNoInteractions(scheduledTaskManager);
  }

  @Test
  void synchronize_shouldOnlyAffectChangedTask_whenManyTasksAreSynchronized() {
    var pingTask = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var telnetTask = TestMonitoringTaskFactory.createTelnetMonitoringTask(2L);

    monitoringTaskService.synchronize(List.of(pingTask, telnetTask));
    monitoringTaskService.synchronize(List.of(pingTask));

    verify(scheduledTaskManager, times(2)).schedule(anyLong(), any());
    verify(scheduledTaskManager, times(1)).cancel(scheduledFuture);
  }

  @Test
  void synchronize_shouldThrow_whenHandlerForTaskTypeDoesNotExist() {
    var task = withType(TestMonitoringTaskFactory.createPingMonitoringTask(1L));

    assertThrows(
        IllegalStateException.class, () -> monitoringTaskService.synchronize(List.of(task)));
    verify(scheduledTaskManager, never()).schedule(anyLong(), any());
  }

  private static MonitoringTask disabled(MonitoringTask task) {
    return new MonitoringTask(
        task.id(),
        task.deviceId(),
        task.deviceIp(),
        task.type(),
        task.interval(),
        false,
        task.configuration(),
        task.updatedAt());
  }

  private static MonitoringTask withUpdatedAt(MonitoringTask task, java.time.Instant updatedAt) {
    return new MonitoringTask(
        task.id(),
        task.deviceId(),
        task.deviceIp(),
        task.type(),
        task.interval(),
        task.enabled(),
        task.configuration(),
        updatedAt);
  }

  private static MonitoringTask withType(MonitoringTask task) {
    return new MonitoringTask(
        task.id(),
        task.deviceId(),
        task.deviceIp(),
        TaskType.HTTP_HEALTHCHECK,
        task.interval(),
        task.enabled(),
        task.configuration(),
        task.updatedAt());
  }
}
