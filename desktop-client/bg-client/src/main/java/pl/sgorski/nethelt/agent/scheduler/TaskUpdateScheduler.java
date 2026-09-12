package pl.sgorski.nethelt.agent.scheduler;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.mapper.MonitoringTaskMapper;
import pl.sgorski.nethelt.agent.service.MonitoringTaskService;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringTaskClient;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TaskUpdateScheduler {

  private final MonitoringTaskClient monitoringTaskClient;
  private final MonitoringTaskMapper monitoringTaskMapper;
  private final MonitoringTaskService monitoringTaskService;

  @Scheduled(fixedDelayString = "${scheduler.update-interval-seconds}", timeUnit = TimeUnit.SECONDS)
  void updateTasks() {
    try {
      var tasks =
          monitoringTaskClient.fetchMonitoringTasks().stream()
              .map(monitoringTaskMapper::toModel)
              .toList();
      tasks.forEach(task -> log.info("task: {}", task));
      monitoringTaskService.synchronize(tasks);
    } catch (Exception e) {
      log.error("Error while updating tasks: {}", e.getMessage(), e);
    }
  }
}
