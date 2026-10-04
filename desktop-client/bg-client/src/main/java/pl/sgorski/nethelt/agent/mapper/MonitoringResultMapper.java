package pl.sgorski.nethelt.agent.mapper;

import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.model.monitoring_result.HttpHealthcheckResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.MonitoringResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.*;
import pl.sgorski.nethelt.agent.webclient.dto.request.*;
import pl.sgorski.nethelt.agent.webclient.dto.response.*;

@Component
public class MonitoringResultMapper {

  public MonitoringResultRequest toRequest(MonitoringResult result) {
    return new MonitoringResultRequest(
        result.getTaskId(),
        result.getTimestamp(),
        result.isSuccess(),
        result.getMessage(),
        result.getResponseTimeMs(),
        getExtensionRequest(result));
  }

  private MonitoringResultExtensionRequest getExtensionRequest(MonitoringResult result) {
    return switch (result) {
      case PingResult _ -> new PingResultExtensionRequest();
      case TelnetResult r -> new TelnetResultExtensionRequest(r.isPortOpen());
      case HttpHealthcheckResult r -> new HttpHealthcheckResultExtensionRequest(r.getStatusCode());
    };
  }
}
