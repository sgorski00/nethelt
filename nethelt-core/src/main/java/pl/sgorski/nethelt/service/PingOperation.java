package pl.sgorski.nethelt.core.service;

import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.PingResult;

/** Represents a network operation that can be performed on a Device, returning a PingResult. */
public interface PingOperation extends AsyncNetworkOperation<Device, PingResult> { }
