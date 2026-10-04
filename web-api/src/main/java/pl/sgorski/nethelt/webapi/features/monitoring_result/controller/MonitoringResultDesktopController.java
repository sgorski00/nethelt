package pl.sgorski.nethelt.webapi.features.monitoring_result.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.MonitoringResultAddRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.webapi.features.monitoring_result.service.MonitoringResultService;
import pl.sgorski.nethelt.webapi.security.agent.AgentAuthentication;

@RestController
@RequestMapping(value = "/client/monitoring-results", version = "1")
@RequiredArgsConstructor
public class MonitoringResultDesktopController {

  private final MonitoringResultService monitoringResultService;
  private final MonitoringResultMapper monitoringResultMapper;

  @PostMapping
  public ResponseEntity<Void> sendResult(
      @RequestBody @Valid MonitoringResultAddRequest request, AgentAuthentication authentication) {
    var command = monitoringResultMapper.toCommand(request);
    monitoringResultService.addMonitoringResult(
        authentication.getPrincipal().networkId(), request.taskId(), command);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
