package pl.sgorski.nethelt.webapi.exception.domain.monitoring_task;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public final class MonitoringTaskValidationFailedException extends ValidationFailedException {
  public MonitoringTaskValidationFailedException(String cause) {
    super("Monitoring task validation failed: " + cause);
  }
}
