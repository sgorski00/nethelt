package pl.sgorski.nethelt.webapi.features.discovery.dto.response;

import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;

public record DiscoveryScanStatusResponse(Long networkId, DiscoveryScanStatus status) {}
