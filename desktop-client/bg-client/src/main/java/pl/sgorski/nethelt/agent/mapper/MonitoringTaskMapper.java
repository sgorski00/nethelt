package pl.sgorski.nethelt.agent.mapper;

import java.net.InetAddress;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.model.monitoring_task.*;
import pl.sgorski.nethelt.agent.webclient.dto.response.*;
import pl.sgorski.nethelt.agent.webclient.dto.response.HttpHealthcheckTaskConfigurationResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.PingTaskConfigurationResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.TelnetTaskConfigurationResponse;

@Component
public class MonitoringTaskMapper {

  public MonitoringTask toModel(MonitoringTaskResponse response) {
    return new MonitoringTask(
        response.id(),
        response.deviceId(),
        InetAddress.ofLiteral(response.deviceIp()),
        toTaskType(response.type()),
        response.interval(),
        response.isEnabled(),
        toConfiguration(response.configuration()),
        response.updatedAt());
  }

  private TaskType toTaskType(TaskType type) {
    return TaskType.valueOf(type.name());
  }

  private MonitoringTaskConfiguration toConfiguration(
      MonitoringTaskConfigurationResponse configuration) {
    return switch (configuration) {
      case PingTaskConfigurationResponse c -> new PingTaskConfiguration(c.timeout());
      case TelnetTaskConfigurationResponse c -> new TelnetTaskConfiguration(c.port(), c.timeout());
      case HttpHealthcheckTaskConfigurationResponse c ->
          new HttpHealthcheckTaskConfiguration(
              c.scheme(), c.port(), c.path(), c.host(), c.expectedStatusCode(), c.timeout());
    };
  }
}
