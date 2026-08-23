package pl.sgorski.nethelt.agent.network.ping;

import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.network.NetworkOperation;

public interface PingOperation extends NetworkOperation<Device, PingResult> {}
