package pl.sgorski.nethelt.webapi.features.monitoring_result.service;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_result.MonitoringResultValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.HttpHealthcheckResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.TelnetResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.HttpHealthcheckResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.PingResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.TelnetResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

@Service
public final class MonitoringResultExtensionService {

  public MonitoringResultExtension mapToExtension(
      TaskType type, MonitoringResultExtensionCommand extension) {
    return switch (type) {
      case PING -> createPingResultExtension(extension);
      case TELNET -> createTelnetResultExtension(extension);
      case HTTP_HEALTHCHECK -> createHttpHealthcheckResultExtension(extension);
    };
  }

  private PingResultExtension createPingResultExtension(
      MonitoringResultExtensionCommand extension) {
    if (extension instanceof PingResultExtensionCommand()) {
      return new PingResultExtension();
    }
    throw new MonitoringResultValidationFailedException(
        "Invalid extension for PING measurement result");
  }

  private TelnetResultExtension createTelnetResultExtension(
      MonitoringResultExtensionCommand extension) {
    if (extension instanceof TelnetResultExtensionCommand(boolean portOpen)) {
      return new TelnetResultExtension(portOpen);
    }
    throw new MonitoringResultValidationFailedException(
        "Invalid extension for TELNET measurement result");
  }

  private HttpHealthcheckResultExtension createHttpHealthcheckResultExtension(
      MonitoringResultExtensionCommand extension) {
    if (extension instanceof HttpHealthcheckResultExtensionCommand(@Nullable Integer statusCode)) {
      return new HttpHealthcheckResultExtension(statusCode);
    }
    throw new MonitoringResultValidationFailedException(
        "Invalid extension for HTTP HEALTHCHECK measurement result");
  }
}
