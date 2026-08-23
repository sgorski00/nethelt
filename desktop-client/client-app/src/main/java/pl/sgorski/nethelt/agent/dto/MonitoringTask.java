package pl.sgorski.nethelt.agent.dto;

import java.util.concurrent.Future;
import pl.sgorski.nethelt.agent.model.Device;

public record MonitoringTask<T>(Device device, Future<T> future) {}
