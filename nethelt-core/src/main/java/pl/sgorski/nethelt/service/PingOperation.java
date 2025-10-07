package pl.sgorski.nethelt.service;

import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;

/** Represents a network operation that can be performed on a Device, returning a PingResult. */
public interface PingOperation extends AsyncNetworkOperation<Device, PingResult> { }
