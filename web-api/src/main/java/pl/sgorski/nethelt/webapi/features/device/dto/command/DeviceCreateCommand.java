package pl.sgorski.nethelt.webapi.features.device.dto.command;

import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;

public record DeviceCreateCommand(Long networkId, String name, String ipAddress, DeviceType type) {}
