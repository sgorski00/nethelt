package pl.sgorski.nethelt.agent.model.discovery;

import java.net.Inet4Address;
import org.jspecify.annotations.Nullable;

public record DiscoveredDevice(
    Inet4Address ipAddress, @Nullable String hostname, long responseTimeMs) {}
