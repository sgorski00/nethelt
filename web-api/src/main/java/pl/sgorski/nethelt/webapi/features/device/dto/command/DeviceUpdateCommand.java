package pl.sgorski.nethelt.webapi.features.device.dto.command;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;

public record DeviceUpdateCommand(
    @Nullable String name, @Nullable String ipAddress, @Nullable DeviceType type) {}
