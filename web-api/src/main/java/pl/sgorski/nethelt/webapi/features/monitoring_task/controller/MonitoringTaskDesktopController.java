package pl.sgorski.nethelt.webapi.features.monitoring_task.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskClientResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_task.mapper.MonitoringTaskMapper;
import pl.sgorski.nethelt.webapi.features.monitoring_task.service.MonitoringTaskService;
import pl.sgorski.nethelt.webapi.security.agent.AgentAuthentication;

@RestController
@RequestMapping(value = "/client/monitoring-tasks", version = "1")
@RequiredArgsConstructor
public class MonitoringTaskDesktopController {

  private final MonitoringTaskService monitoringTaskService;
  private final MonitoringTaskMapper monitoringTaskMapper;

  @GetMapping
  public ResponseEntity<List<MonitoringTaskClientResponse>> retrieveAllTasks(
      AgentAuthentication authentication) {
    var result =
        monitoringTaskService
            .getActiveMonitoringTasks(authentication.getPrincipal().networkId())
            .stream()
            .map(monitoringTaskMapper::toClientResponse)
            .toList();
    return ResponseEntity.ok(result);
  }
}
