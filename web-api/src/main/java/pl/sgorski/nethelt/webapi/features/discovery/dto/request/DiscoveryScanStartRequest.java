package pl.sgorski.nethelt.webapi.features.discovery.dto.request;

import org.jspecify.annotations.Nullable;

/**
 * @param ipRange a subnet, a range or a single address, e.g. {@code 192.168.0.0/24}; when empty, the agent scans its own subnet
 */
public record DiscoveryScanStartRequest(@Nullable String ipRange) {}
