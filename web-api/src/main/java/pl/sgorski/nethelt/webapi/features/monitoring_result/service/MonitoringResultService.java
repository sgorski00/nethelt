package pl.sgorski.nethelt.webapi.features.monitoring_result.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_result.MonitoringResultNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_result.MonitoringResultValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultBatchSummary;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultKey;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultAddCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultBatchItemCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.repository.MonitoringResultRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.service.MonitoringTaskService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringResultService {

  private final MonitoringResultRepository monitoringResultRepository;
  private final MonitoringResultExtensionService monitoringResultExtensionService;
  private final MonitoringTaskService taskService;

  public MonitoringResult getMonitoringResult(
      Long networkId, Long deviceId, Long monitoringTaskId, Long monitoringResultId) {
    var task = taskService.getMonitoringTask(networkId, deviceId, monitoringTaskId);
    return monitoringResultRepository
        .findByTaskAndId(task, monitoringResultId)
        .orElseThrow(MonitoringResultNotFoundException::new);
  }

  public Page<MonitoringResult> getMonitoringResults(
      Long networkId,
      Long deviceId,
      Long monitoringTaskId,
      @Nullable Instant from,
      @Nullable Instant to,
      Pageable pageable) {
    var task = taskService.getMonitoringTask(networkId, deviceId, monitoringTaskId);
    return monitoringResultRepository.findAllByTaskAndExecutedAtBetween(task, from, to, pageable);
  }

  @Transactional
  public MonitoringResult addMonitoringResultIfAbsent(
      Long networkId, Long monitoringTaskId, MonitoringResultAddCommand command) {
    var task = taskService.getMonitoringTask(networkId, monitoringTaskId);
    var result = createMonitoringResult(task, command);
    return monitoringResultRepository
        .findByTaskAndExecutedAt(task, result.getExecutedAt())
        .orElseGet(() -> monitoringResultRepository.save(result));
  }

  @Transactional
  public MonitoringResultBatchSummary addMonitoringResultsIfAbsent(
      Long networkId, List<MonitoringResultBatchItemCommand> commands) {
    var tasksById = findTasksById(networkId, commands);
    var validResults =
        commands.stream()
            .flatMap(command -> createValidMonitoringResult(command, tasksById).stream())
            .toList();
    var newResults = skipDuplicates(validResults);

    monitoringResultRepository.saveAll(newResults);
    return new MonitoringResultBatchSummary(
        newResults.size(),
        validResults.size() - newResults.size(),
        commands.size() - validResults.size());
  }

  private Map<Long, MonitoringTask> findTasksById(
      Long networkId, List<MonitoringResultBatchItemCommand> commands) {
    var taskIds =
        commands.stream().map(MonitoringResultBatchItemCommand::taskId).collect(Collectors.toSet());
    return taskService.findMonitoringTasksByIds(networkId, taskIds).stream()
        .collect(Collectors.toMap(MonitoringTask::getId, Function.identity()));
  }

  private Optional<MonitoringResult> createValidMonitoringResult(
      MonitoringResultBatchItemCommand command, Map<Long, MonitoringTask> tasksById) {
    var task = tasksById.get(command.taskId());
    if (task == null) {
      log.warn("Skipping result of task {}: task not found in the network", command.taskId());
      return Optional.empty();
    }
    try {
      return Optional.of(createMonitoringResult(task, command.result()));
    } catch (MonitoringResultValidationFailedException e) {
      log.warn("Skipping result of task {}: {}", task.getId(), e.getMessage());
      return Optional.empty();
    }
  }

  private List<MonitoringResult> skipDuplicates(List<MonitoringResult> results) {
    var seenKeys = new HashSet<>(findSavedKeys(results));
    return results.stream().filter(result -> seenKeys.add(MonitoringResultKey.of(result))).toList();
  }

  private List<MonitoringResultKey> findSavedKeys(List<MonitoringResult> results) {
    if (results.isEmpty()) return List.of();

    var taskIds =
        results.stream().map(result -> result.getTask().getId()).collect(Collectors.toSet());
    var executedAts = results.stream().map(MonitoringResult::getExecutedAt).toList();
    return monitoringResultRepository.findKeysByTaskIdInAndExecutedAtIn(taskIds, executedAts);
  }

  private MonitoringResult createMonitoringResult(
      MonitoringTask task, MonitoringResultAddCommand command) {
    var extension =
        monitoringResultExtensionService.mapToExtension(task.getType(), command.extension());
    return new MonitoringResult(
        task,
        truncateToDatabasePrecision(command.executedAt()),
        command.success(),
        command.message(),
        command.responseTimeMs(),
        extension);
  }

  private static Instant truncateToDatabasePrecision(Instant executedAt) {
    return executedAt.truncatedTo(ChronoUnit.MICROS);
  }
}
