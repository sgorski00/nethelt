package pl.sgorski.nethelt.agent.service;

import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.TelnetResult;
import pl.sgorski.nethelt.service.AsyncNetworkOperation;

/** Represents a network operation that can be performed on a Device, returning a TelnetResult. */
public interface TelnetOperation extends AsyncNetworkOperation<Device, TelnetResult> {}
