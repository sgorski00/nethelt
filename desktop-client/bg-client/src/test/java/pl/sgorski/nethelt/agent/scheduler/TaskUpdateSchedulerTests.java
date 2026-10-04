package pl.sgorski.nethelt.agent.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.mapper.MonitoringTaskMapper;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.service.MonitoringTaskService;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringTaskClient;
import pl.sgorski.nethelt.agent.webclient.dto.response.MonitoringTaskResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.PingTaskConfigurationResponse;

@ExtendWith(MockitoExtension.class)
public class TaskUpdateSchedulerTests {

  @Mock private MonitoringTaskClient monitoringTaskClient;
  @Mock private MonitoringTaskMapper monitoringTaskMapper;
  @Mock private MonitoringTaskService monitoringTaskService;
  @InjectMocks private TaskUpdateScheduler taskUpdateScheduler;

  @Test
  void updateTasks_shouldSynchronizeFetchedTasks() {
    var response =
        new MonitoringTaskResponse(
            1L,
            TaskType.PING,
            1L,
            "127.0.0.1",
            Duration.ofSeconds(30),
            true,
            new PingTaskConfigurationResponse(Duration.ofSeconds(5)),
            Instant.now());
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    when(monitoringTaskClient.fetchMonitoringTasks()).thenReturn(Set.of(response));
    when(monitoringTaskMapper.toModel(response)).thenReturn(task);

    taskUpdateScheduler.updateTasks();

    verify(monitoringTaskService).synchronize(List.of(task));
  }

  @Test
  void updateTasks_shouldSynchronizeEmptyList_whenNoTasksWereFetched() {
    when(monitoringTaskClient.fetchMonitoringTasks()).thenReturn(Set.of());

    taskUpdateScheduler.updateTasks();

    verify(monitoringTaskService).synchronize(List.of());
  }

  @Test
  void updateTasks_shouldNotSynchronize_whenFetchingFails() {
    when(monitoringTaskClient.fetchMonitoringTasks()).thenThrow(new WebClientException("failed"));

    assertDoesNotThrow(() -> taskUpdateScheduler.updateTasks());
    verifyNoInteractions(monitoringTaskService);
  }

  @Test
  void updateTasks_shouldNotSynchronize_whenMappingFails() {
    var response =
        new MonitoringTaskResponse(
            1L,
            TaskType.PING,
            1L,
            "not-an-ip",
            Duration.ofSeconds(30),
            true,
            new PingTaskConfigurationResponse(Duration.ofSeconds(5)),
            Instant.now());
    when(monitoringTaskClient.fetchMonitoringTasks()).thenReturn(Set.of(response));
    when(monitoringTaskMapper.toModel(response)).thenThrow(new IllegalArgumentException());

    assertDoesNotThrow(() -> taskUpdateScheduler.updateTasks());
    verifyNoInteractions(monitoringTaskService);
  }
}
