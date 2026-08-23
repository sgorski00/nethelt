package pl.sgorski.nethelt.agent.network;

import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.Result;

public interface NetworkOperation<R extends Result> {
  R execute(Device device) throws NetworkException;

  R error(Device device);
}
