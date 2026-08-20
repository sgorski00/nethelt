package pl.sgorski.nethelt.agent.service;

import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.Result;

public interface NetworkOperation<I extends Device, O extends Result> {
  O execute(I device) throws NetworkException;
}
