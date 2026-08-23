package pl.sgorski.nethelt.agent.service;

import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.Result;

public interface NetworkOperation<I extends Device, O extends Result> {
  O execute(I device) throws NetworkException;
}
