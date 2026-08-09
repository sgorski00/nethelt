package pl.sgorski.nethelt.webapi.features.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;

public record DeviceUpdateRequest(
    @NotBlank(message = "Device name cannot be empty") String name,
    @NotBlank(message = "Device ip address cannot be empty") String ipAddress,
    @NotNull(message = "Device type cannot be empty") DeviceType type) {}
