package pl.sgorski.nethelt.webapi.features.monitoring_result.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_result.MonitoringResultValidationFailedException;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskNotFoundException;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultBatchSummary;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultKey;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultAddCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultBatchItemCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.PingResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.TelnetResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.repository.MonitoringResultRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
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
  void addMonitoringResultIfAbsent_shouldSaveResult_whenTaskInNetwork() {
    var task = TestMonitoringTaskFactory.createTask();
    var executedAt = Instant.parse("2026-11-04T12:00:00.123456Z");
    var extensionCommand = new PingResultExtensionCommand();
    var extension = new PingResultExtension();
    var command = new MonitoringResultAddCommand(executedAt, true, "OK", 12L, extensionCommand);
    when(taskService.getMonitoringTask(2L, 1L)).thenReturn(task);
    when(monitoringResultExtensionService.mapToExtension(TaskType.PING, extensionCommand))
        .thenReturn(extension);
    when(monitoringResultRepository.save(any(MonitoringResult.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = monitoringResultService.addMonitoringResultIfAbsent(2L, 1L, command);

    assertSame(task, result.getTask());
    assertEquals(executedAt, result.getExecutedAt());
    assertTrue(result.isSuccess());
    assertEquals("OK", result.getMessage());
    assertEquals(12L, result.getResponseTimeMs());
    assertSame(extension, result.getExtension());
  }

  @Test
  void addMonitoringResultIfAbsent_shouldThrowException_whenTaskNotInNetwork() {
    var command =
        new MonitoringResultAddCommand(
            Instant.now(), false, null, null, new PingResultExtensionCommand());
    when(taskService.getMonitoringTask(2L, 1L)).thenThrow(new MonitoringTaskNotFoundException());

    assertThrows(
        MonitoringTaskNotFoundException.class,
        () -> monitoringResultService.addMonitoringResultIfAbsent(2L, 1L, command));
    verifyNoInteractions(monitoringResultRepository);
  }

  @Test
  void addMonitoringResultIfAbsent_shouldReturnSavedResult_andNotSaveAgain_whenResultIsResent() {
    var task = TestMonitoringTaskFactory.createTask();
    var executedAt = Instant.parse("2026-11-04T12:00:00Z");
    var extensionCommand = new PingResultExtensionCommand();
    var command = new MonitoringResultAddCommand(executedAt, true, "OK", 12L, extensionCommand);
    var saved = mock(MonitoringResult.class);
    when(taskService.getMonitoringTask(2L, 1L)).thenReturn(task);
    when(monitoringResultExtensionService.mapToExtension(TaskType.PING, extensionCommand))
        .thenReturn(new PingResultExtension());
    when(monitoringResultRepository.findByTaskAndExecutedAt(task, executedAt))
        .thenReturn(Optional.of(saved));

    var result = monitoringResultService.addMonitoringResultIfAbsent(2L, 1L, command);

    assertSame(saved, result);
    verify(monitoringResultRepository, never()).save(any());
  }

  @Test
  void addMonitoringResultIfAbsent_shouldTruncateExecutedAtToMicros() {
    var task = TestMonitoringTaskFactory.createTask();
    var extensionCommand = new PingResultExtensionCommand();
    var command =
        new MonitoringResultAddCommand(
            Instant.parse("2026-11-04T12:00:00.123456789Z"), true, "OK", 12L, extensionCommand);
    var executedAt = Instant.parse("2026-11-04T12:00:00.123456Z");
    when(taskService.getMonitoringTask(2L, 1L)).thenReturn(task);
    when(monitoringResultExtensionService.mapToExtension(TaskType.PING, extensionCommand))
        .thenReturn(new PingResultExtension());
    when(monitoringResultRepository.save(any(MonitoringResult.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = monitoringResultService.addMonitoringResultIfAbsent(2L, 1L, command);

    assertEquals(executedAt, result.getExecutedAt());
    verify(monitoringResultRepository).findByTaskAndExecutedAt(task, executedAt);
  }

  @Test
  void addMonitoringResultsIfAbsent_shouldSaveAllResults_whenTheyAreNew() {
    var task = createTask(1L);
    var first = createBatchItem(1L, Instant.parse("2026-11-04T12:00:00Z"));
    var second = createBatchItem(1L, Instant.parse("2026-11-04T12:01:00Z"));
    when(taskService.findMonitoringTasksByIds(2L, Set.of(1L))).thenReturn(List.of(task));
    when(monitoringResultExtensionService.mapToExtension(eq(TaskType.PING), any()))
        .thenAnswer(_ -> new PingResultExtension());

    var summary = monitoringResultService.addMonitoringResultsIfAbsent(2L, List.of(first, second));

    assertEquals(new MonitoringResultBatchSummary(2, 0, 0), summary);
    verify(monitoringResultRepository)
        .saveAll(
            argThat(
                (List<MonitoringResult> results) ->
                    results.size() == 2
                        && results.get(0).getExecutedAt().equals(first.result().executedAt())
                        && results.get(1).getExecutedAt().equals(second.result().executedAt())
                        && results.get(0).getTask() == task));
  }

  @Test
  void addMonitoringResultsIfAbsent_shouldSkipResults_thatAreAlreadySaved() {
    var task = createTask(1L);
    var executedAt = Instant.parse("2026-11-04T12:00:00Z");
    when(taskService.findMonitoringTasksByIds(2L, Set.of(1L))).thenReturn(List.of(task));
    when(monitoringResultExtensionService.mapToExtension(eq(TaskType.PING), any()))
        .thenAnswer(_ -> new PingResultExtension());
    when(monitoringResultRepository.findKeysByTaskIdInAndExecutedAtIn(any(), any()))
        .thenReturn(List.of(new MonitoringResultKey(1L, executedAt)));

    var summary =
        monitoringResultService.addMonitoringResultsIfAbsent(
            2L,
            List.of(
                createBatchItem(1L, executedAt),
                createBatchItem(1L, Instant.parse("2026-11-04T12:01:00Z"))));

    assertEquals(new MonitoringResultBatchSummary(1, 1, 0), summary);
  }

  @Test
  void addMonitoringResultsIfAbsent_shouldSkipResults_repeatedInBatch() {
    var task = createTask(1L);
    when(taskService.findMonitoringTasksByIds(2L, Set.of(1L))).thenReturn(List.of(task));
    when(monitoringResultExtensionService.mapToExtension(eq(TaskType.PING), any()))
        .thenAnswer(_ -> new PingResultExtension());

    var summary =
        monitoringResultService.addMonitoringResultsIfAbsent(
            2L,
            List.of(
                createBatchItem(1L, Instant.parse("2026-11-04T12:00:00.123456Z")),
                createBatchItem(1L, Instant.parse("2026-11-04T12:00:00.123456789Z"))));

    assertEquals(new MonitoringResultBatchSummary(1, 1, 0), summary);
  }

  @Test
  void addMonitoringResultsIfAbsent_shouldRejectResults_ofUnknownTasks() {
    var task = createTask(1L);
    when(taskService.findMonitoringTasksByIds(2L, Set.of(1L, 99L))).thenReturn(List.of(task));
    when(monitoringResultExtensionService.mapToExtension(eq(TaskType.PING), any()))
        .thenAnswer(_ -> new PingResultExtension());

    var summary =
        monitoringResultService.addMonitoringResultsIfAbsent(
            2L,
            List.of(
                createBatchItem(99L, Instant.parse("2026-11-04T12:00:00Z")),
                createBatchItem(1L, Instant.parse("2026-11-04T12:00:00Z"))));

    assertEquals(new MonitoringResultBatchSummary(1, 0, 1), summary);
  }

  @Test
  void addMonitoringResultsIfAbsent_shouldRejectResults_withInvalidExtension() {
    var task = createTask(1L);
    var invalid =
        new MonitoringResultBatchItemCommand(
            1L,
            new MonitoringResultAddCommand(
                Instant.parse("2026-11-04T12:00:00Z"),
                true,
                null,
                null,
                new TelnetResultExtensionCommand(true)));
    var valid = createBatchItem(1L, Instant.parse("2026-11-04T12:01:00Z"));
    when(taskService.findMonitoringTasksByIds(2L, Set.of(1L))).thenReturn(List.of(task));
    when(monitoringResultExtensionService.mapToExtension(
            TaskType.PING, invalid.result().extension()))
        .thenThrow(new MonitoringResultValidationFailedException("invalid"));
    when(monitoringResultExtensionService.mapToExtension(TaskType.PING, valid.result().extension()))
        .thenReturn(new PingResultExtension());

    var summary = monitoringResultService.addMonitoringResultsIfAbsent(2L, List.of(invalid, valid));

    assertEquals(new MonitoringResultBatchSummary(1, 0, 1), summary);
  }

  @Test
  void addMonitoringResultsIfAbsent_shouldNotQueryResults_whenNoTaskIsFound() {
    when(taskService.findMonitoringTasksByIds(2L, Set.of(99L))).thenReturn(List.of());

    var summary =
        monitoringResultService.addMonitoringResultsIfAbsent(
            2L, List.of(createBatchItem(99L, Instant.parse("2026-11-04T12:00:00Z"))));

    assertEquals(new MonitoringResultBatchSummary(0, 0, 1), summary);
    verify(monitoringResultRepository, never()).findKeysByTaskIdInAndExecutedAtIn(any(), any());
  }

  private static MonitoringTask createTask(Long id) {
    var task = TestMonitoringTaskFactory.createTask(TaskType.PING, Duration.ofSeconds(10));
    ReflectionTestUtils.setField(task, "id", id);
    return task;
  }

  private static MonitoringResultBatchItemCommand createBatchItem(Long taskId, Instant executedAt) {
    return new MonitoringResultBatchItemCommand(
        taskId,
        new MonitoringResultAddCommand(
            executedAt, true, "OK", 12L, new PingResultExtensionCommand()));
  }
}
