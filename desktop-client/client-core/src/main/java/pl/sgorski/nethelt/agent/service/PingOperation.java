package pl.sgorski.nethelt.agent.service;

import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.service.AsyncNetworkOperation;

/** Represents a network operation that can be performed on a Device, returning a PingResult. */
public interface PingOperation extends AsyncNetworkOperation<Device, PingResult> {}
