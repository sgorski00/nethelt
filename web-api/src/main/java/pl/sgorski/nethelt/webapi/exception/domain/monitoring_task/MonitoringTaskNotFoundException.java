package pl.sgorski.nethelt.webapi.exception.domain.monitoring_task;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class MonitoringTaskNotFoundException extends NotFoundException {
  public MonitoringTaskNotFoundException() {
    super("Monitoring task not found");
  }
}
