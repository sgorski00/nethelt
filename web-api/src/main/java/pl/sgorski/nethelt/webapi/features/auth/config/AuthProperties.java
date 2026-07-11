package pl.sgorski.nethelt.webapi.features.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
    Duration refreshTokenExpiration,
    Duration oauth2ContextExpiration,
    Duration oauth2AuthorizationRequestExpiration,
    Duration jwtTokenExpiration,
    String jwtSecretKey) {}
