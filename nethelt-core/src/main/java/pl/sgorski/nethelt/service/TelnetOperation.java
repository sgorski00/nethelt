package pl.sgorski.nethelt.core.service;

import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.TelnetResult;

/** Represents a network operation that can be performed on a Device, returning a TelnetResult. */
public interface TelnetOperation extends AsyncNetworkOperation<Device, TelnetResult> { }
