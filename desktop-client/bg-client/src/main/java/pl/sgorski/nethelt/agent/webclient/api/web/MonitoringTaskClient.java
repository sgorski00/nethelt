package pl.sgorski.nethelt.agent.webclient.api.web;

import java.util.Set;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import pl.sgorski.nethelt.agent.webclient.dto.response.MonitoringTaskResponse;

@HttpExchange(url = "/client/monitoring-tasks")
public interface MonitoringTaskClient {

  @GetExchange
  Set<MonitoringTaskResponse> fetchMonitoringTasks();
}
