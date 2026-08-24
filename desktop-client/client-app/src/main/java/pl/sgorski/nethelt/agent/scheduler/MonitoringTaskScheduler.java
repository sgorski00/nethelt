package pl.sgorski.nethelt.agent.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitoringTaskScheduler {

  private final TaskScheduler scheduler;

  public ScheduledFuture<?> schedule(int intervalSeconds, Runnable task) {
    return scheduler.scheduleWithFixedDelay(
        task, Instant.now(), Duration.ofSeconds(intervalSeconds));
  }

  public void close(ScheduledFuture<?> task) {
    if (task != null && task.isCancelled()) {
      task.cancel(false);
    }
  }
}
