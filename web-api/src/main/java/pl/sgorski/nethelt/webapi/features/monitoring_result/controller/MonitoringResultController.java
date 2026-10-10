package pl.sgorski.nethelt.webapi.features.monitoring_result.controller;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.MonitoringResultResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.webapi.features.monitoring_result.service.MonitoringResultService;

@RestController
@RequestMapping(
    value = "/networks/{networkId}/devices/{deviceId}/tasks/{taskId}/results",
    version = "1")
@RequiredArgsConstructor
@PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
public class MonitoringResultController {

  private final MonitoringResultService monitoringResultService;
  private final MonitoringResultMapper monitoringResultMapper;

  @GetMapping
  public ResponseEntity<Page<MonitoringResultResponse>> getMonitoringResults(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId,
      @RequestParam(name = "from", required = false) @Nullable Instant from,
      @RequestParam(name = "to", required = false) @Nullable Instant to,
      Pageable pageable) {
    var results =
        monitoringResultService
            .getMonitoringResults(networkId, deviceId, taskId, from, to, pageable)
            .map(monitoringResultMapper::toResponse);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/{resultId}")
  public ResponseEntity<MonitoringResultResponse> getMonitoringResult(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @PathVariable("taskId") Long taskId,
      @PathVariable("resultId") Long resultId) {
    var result = monitoringResultService.getMonitoringResult(networkId, deviceId, taskId, resultId);
    return ResponseEntity.ok(monitoringResultMapper.toResponse(result));
  }
}
