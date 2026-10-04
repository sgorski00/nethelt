package pl.sgorski.nethelt.agent.network;

import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.monitoring_result.MonitoringResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;

public interface NetworkOperation<R extends MonitoringResult> {
  R execute(MonitoringTask task) throws NetworkException;

  R error(MonitoringTask task);
}
