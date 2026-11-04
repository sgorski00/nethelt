package pl.sgorski.nethelt.agent.webclient.api.web;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultBatchRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.MonitoringResultBatchResponse;

@Validated
@HttpExchange(url = "/client/monitoring-results")
public interface MonitoringResultClient {

  @PostExchange
  void sendMonitoringResult(@Valid @RequestBody MonitoringResultRequest request);

  @PostExchange("/batch")
  MonitoringResultBatchResponse sendMonitoringResults(
      @Valid @RequestBody MonitoringResultBatchRequest request);
}
