package pl.sgorski.nethelt.webapi.exception.domain.monitoring_result;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public final class MonitoringResultValidationFailedException extends ValidationFailedException {
  public MonitoringResultValidationFailedException(String cause) {
    super("Monitoring result validation failed: " + cause);
  }
}
