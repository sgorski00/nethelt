package pl.sgorski.nethelt.webapi.features.device.dto.command;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;

public record DeviceFilterCommand(
    @Nullable String name, @Nullable DeviceType type, @Nullable String ip) {}
