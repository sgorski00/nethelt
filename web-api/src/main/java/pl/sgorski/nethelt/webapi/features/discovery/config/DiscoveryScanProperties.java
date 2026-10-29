package pl.sgorski.nethelt.webapi.features.discovery.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discovery.scan")
public record DiscoveryScanProperties(Duration timeout) {}
