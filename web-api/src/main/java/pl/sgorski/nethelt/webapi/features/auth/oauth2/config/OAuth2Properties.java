package pl.sgorski.nethelt.webapi.features.auth.oauth2.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2")
public record OAuth2Properties(
    String successUrl,
    String failureUrl,
    Duration authorizationRequestExpiration,
    Duration contextExpiration) {}
