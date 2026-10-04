package pl.sgorski.nethelt.webapi.exception.domain.monitoring_result;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class MonitoringResultNotFoundException extends NotFoundException {
  public MonitoringResultNotFoundException() {
    super("Monitoring result not found");
  }
}
