package pl.sgorski.nethelt.agent.network.discovery;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discovery")
public record DiscoveryProperties(Duration pingTimeout, int maxParallelProbes) {}
