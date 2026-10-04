package pl.sgorski.nethelt.webapi.features.monitoring_result.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskNotFoundException;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultAddCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.PingResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.repository.MonitoringResultRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.features.monitoring_task.service.MonitoringTaskService;
import pl.sgorski.nethelt.webapi.utils.TestMonitoringTaskFactory;

@ExtendWith(MockitoExtension.class)
public class MonitoringResultServiceTests {

  @Mock private MonitoringResultRepository monitoringResultRepository;
  @Mock private MonitoringResultExtensionService monitoringResultExtensionService;
  @Mock private MonitoringTaskService taskService;
  @InjectMocks private MonitoringResultService monitoringResultService;

  @Test
  void addMonitoringResult_shouldSaveResult_whenTaskInNetwork() {
    var task = TestMonitoringTaskFactory.createTask();
    var executedAt = Instant.now();
    var extensionCommand = new PingResultExtensionCommand();
    var extension = new PingResultExtension();
    var command = new MonitoringResultAddCommand(executedAt, true, "OK", 12L, extensionCommand);
    when(taskService.getMonitoringTask(2L, 1L)).thenReturn(task);
    when(monitoringResultExtensionService.mapToExtension(TaskType.PING, extensionCommand))
        .thenReturn(extension);
    when(monitoringResultRepository.save(any(MonitoringResult.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = monitoringResultService.addMonitoringResult(2L, 1L, command);

    assertSame(task, result.getTask());
    assertEquals(executedAt, result.getExecutedAt());
    assertTrue(result.isSuccess());
    assertEquals("OK", result.getMessage());
    assertEquals(12L, result.getResponseTimeMs());
    assertSame(extension, result.getExtension());
  }

  @Test
  void addMonitoringResult_shouldThrowException_whenTaskNotInNetwork() {
    var command =
        new MonitoringResultAddCommand(
            Instant.now(), false, null, null, new PingResultExtensionCommand());
    when(taskService.getMonitoringTask(2L, 1L)).thenThrow(new MonitoringTaskNotFoundException());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringResultService.addMonitoringResult(2L, 1L, command));
    verifyNoInteractions(monitoringResultRepository);
  }
}
