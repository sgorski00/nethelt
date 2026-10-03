package pl.sgorski.nethelt.agent.network;

import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.monitoring_result.Result;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;

public interface NetworkOperation<R extends Result> {
  R execute(MonitoringTask task) throws NetworkException;

  R error(MonitoringTask task);
}
