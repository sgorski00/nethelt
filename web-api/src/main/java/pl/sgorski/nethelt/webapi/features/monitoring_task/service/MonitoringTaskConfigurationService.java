package pl.sgorski.nethelt.webapi.features.monitoring_task.service;

import java.time.Duration;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.PingTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.TelnetTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.HttpHealthcheckTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.PingTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.TelnetTaskConfigurationCommand;

@Service
public class MonitoringTaskConfigurationService {

  public MonitoringTaskConfiguration createConfiguration(
      TaskType type, MonitoringTaskConfigurationCommand configuration) {
    return switch (type) {
      case PING -> createPingTaskConfiguration(configuration);
      case TELNET -> createTelnetTaskConfiguration(configuration);
      case HTTP_HEALTHCHECK -> createHttpHealthcheckTaskConfiguration(configuration);
    };
  }

  private PingTaskConfiguration createPingTaskConfiguration(
      MonitoringTaskConfigurationCommand configuration) {
    if (configuration instanceof PingTaskConfigurationCommand(long timeoutMs)) {
      var timeout = getDurationFromMillis(timeoutMs);
      return new PingTaskConfiguration(timeout);
    }
    throw new MonitoringTaskValidationFailedException("Invalid configuration for PING task");
  }

  private TelnetTaskConfiguration createTelnetTaskConfiguration(
      MonitoringTaskConfigurationCommand configuration) {
    if (configuration instanceof TelnetTaskConfigurationCommand(int port, long timeoutMs)) {
      var timeout = getDurationFromMillis(timeoutMs);
      return new TelnetTaskConfiguration(port, timeout);
    }
    throw new MonitoringTaskValidationFailedException("Invalid configuration for TELNET task");
  }

  private HttpHealthcheckTaskConfiguration createHttpHealthcheckTaskConfiguration(
      MonitoringTaskConfigurationCommand configuration) {
    if (configuration
        instanceof HttpHealthcheckTaskConfigurationCommand(int port, String path, long timeoutMs)) {
      var timeout = getDurationFromMillis(timeoutMs);
      return new HttpHealthcheckTaskConfiguration(port, path, timeout);
    }
    throw new MonitoringTaskValidationFailedException(
        "Invalid configuration for HTTP HEALTHCHECK task");
  }

  private Duration getDurationFromMillis(long millis) {
    if (millis <= 0) {
      throw new MonitoringTaskValidationFailedException("Timeout must be greater than 0");
    }
    return Duration.ofMillis(millis);
  }
}
