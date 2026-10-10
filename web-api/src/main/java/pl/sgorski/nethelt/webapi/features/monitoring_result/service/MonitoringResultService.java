package pl.sgorski.nethelt.webapi.features.monitoring_result.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_result.MonitoringResultNotFoundException;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultAddCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.repository.MonitoringResultRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.service.MonitoringTaskService;

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
  public MonitoringResult addMonitoringResult(
      Long networkId, Long monitoringTaskId, MonitoringResultAddCommand command) {
    var task = taskService.getMonitoringTask(networkId, monitoringTaskId);
    var extension =
        monitoringResultExtensionService.mapToExtension(task.getType(), command.extension());
    var monitoringResult =
        new MonitoringResult(
            task,
            command.executedAt(),
            command.success(),
            command.message(),
            command.responseTimeMs(),
            extension);
    return monitoringResultRepository.save(monitoringResult);
  }
}
