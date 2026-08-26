package pl.sgorski.nethelt.agent.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
public class MonitoringTaskSchedulerTests {

  @Mock private TaskScheduler scheduler;
  @InjectMocks private MonitoringTaskScheduler monitoringTaskScheduler;

  @Test
  void schedule_shouldScheduleTaskWithFixedDelay() {
    var intervalSeconds = 10;
    Runnable task = () -> {};

    monitoringTaskScheduler.schedule(intervalSeconds, task);

    verify(scheduler)
        .scheduleWithFixedDelay(
            eq(task),
            argThat(instant -> instant.isBefore(Instant.now())),
            eq(Duration.ofSeconds(intervalSeconds)));
  }

  @Test
  void cancel_shouldCancelScheduledTask() {
    var scheduledFuture = mock(ScheduledFuture.class);
    when(scheduledFuture.isCancelled()).thenReturn(false);

    monitoringTaskScheduler.cancel(scheduledFuture);

    verify(scheduledFuture).cancel(false);
  }

  @Test
  void cancel_shouldNotCancelScheduledTask_whenTaskIsCancelled() {
    var scheduledFuture = mock(ScheduledFuture.class);
    when(scheduledFuture.isCancelled()).thenReturn(true);

    monitoringTaskScheduler.cancel(scheduledFuture);

    verify(scheduledFuture, never()).cancel(anyBoolean());
  }
}
