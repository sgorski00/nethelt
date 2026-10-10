package pl.sgorski.nethelt.webapi.features.metrics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.metrics.dto.param.MetricsRange;
import pl.sgorski.nethelt.webapi.features.metrics.dto.response.MetricsResponse;
import pl.sgorski.nethelt.webapi.features.metrics.mapper.MetricsMapper;
import pl.sgorski.nethelt.webapi.features.metrics.service.MetricsService;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

@RestController
@RequestMapping(value = "/networks/{networkId}/metrics", version = "1")
@RequiredArgsConstructor
@PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
public class MetricsController {

  private final MetricsService metricsService;
  private final MetricsMapper metricsMapper;

  /** {@code range} is bound from the optional {@code from} and {@code to} query parameters. */
  @GetMapping
  public ResponseEntity<MetricsResponse> getMetrics(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestParam(name = "deviceId", required = false) @Nullable Long deviceId,
      @RequestParam(name = "taskId", required = false) @Nullable Long taskId,
      @RequestParam(name = "type", required = false) @Nullable TaskType type,
      @Valid @ModelAttribute MetricsRange range) {
    var metrics =
        metricsService.getMetrics(networkId, deviceId, taskId, type, range.from(), range.to());
    return ResponseEntity.ok(metricsMapper.toResponse(metrics));
  }
}
