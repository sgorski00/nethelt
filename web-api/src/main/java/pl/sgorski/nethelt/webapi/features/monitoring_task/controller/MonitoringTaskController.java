package pl.sgorski.nethelt.webapi.features.monitoring_task.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskCreateRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskUpdateRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_task.mapper.MonitoringTaskMapper;
import pl.sgorski.nethelt.webapi.features.monitoring_task.service.MonitoringTaskService;

@RestController
@RequestMapping(value = "/networks/{networkId}/devices/{deviceId}/tasks", version = "1")
@RequiredArgsConstructor
@PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
public class MonitoringTaskController {

  private final MonitoringTaskService monitoringTaskService;
  private final MonitoringTaskMapper monitoringTaskMapper;

  @GetMapping
  public ResponseEntity<List<MonitoringTaskResponse>> getMonitoringTasks(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId) {
    var tasks =
        monitoringTaskService.getMonitoringTasks(networkId, deviceId).stream()
            .map(monitoringTaskMapper::toResponse)
            .toList();
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/{taskId}")
  public ResponseEntity<MonitoringTaskResponse> getMonitoringTask(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId) {
    var task = monitoringTaskService.getMonitoringTask(networkId, deviceId, taskId);
    return ResponseEntity.ok(monitoringTaskMapper.toResponse(task));
  }

  @PostMapping
  public ResponseEntity<MonitoringTaskResponse> createMonitoringTask(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @RequestBody @Valid MonitoringTaskCreateRequest request) {
    var command = monitoringTaskMapper.toCommand(request);
    var task = monitoringTaskService.createMonitoringTask(networkId, deviceId, command);
    return ResponseEntity.status(HttpStatus.CREATED).body(monitoringTaskMapper.toResponse(task));
  }

  @PutMapping("/{taskId}")
  public ResponseEntity<MonitoringTaskResponse> updateMonitoringTask(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId,
      @RequestBody @Valid MonitoringTaskUpdateRequest request) {
    var command = monitoringTaskMapper.toCommand(request);
    var task = monitoringTaskService.updateMonitoringTask(networkId, deviceId, taskId, command);
    return ResponseEntity.ok(monitoringTaskMapper.toResponse(task));
  }

  @PatchMapping("/{taskId}/enable")
  public ResponseEntity<MonitoringTaskResponse> enableMonitoringTask(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId) {
    var task = monitoringTaskService.enableMonitoringTask(networkId, deviceId, taskId);
    return ResponseEntity.ok(monitoringTaskMapper.toResponse(task));
  }

  @PatchMapping("/{taskId}/disable")
  public ResponseEntity<MonitoringTaskResponse> disableMonitoringTask(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId) {
    var task = monitoringTaskService.disableMonitoringTask(networkId, deviceId, taskId);
    return ResponseEntity.ok(monitoringTaskMapper.toResponse(task));
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteMonitoringTask(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId) {
    monitoringTaskService.deleteMonitoringTask(networkId, deviceId, taskId);
    return ResponseEntity.noContent().build();
  }
}
