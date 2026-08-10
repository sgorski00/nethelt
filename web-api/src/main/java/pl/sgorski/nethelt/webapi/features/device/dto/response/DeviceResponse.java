package pl.sgorski.nethelt.webapi.features.device.dto.response;

import java.time.Instant;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;

public record DeviceResponse(
    Long id,
    String name,
    String ipAddress,
    DeviceType type,
    boolean isEnabled,
    Instant createdAt,
    Instant updatedAt) {}
