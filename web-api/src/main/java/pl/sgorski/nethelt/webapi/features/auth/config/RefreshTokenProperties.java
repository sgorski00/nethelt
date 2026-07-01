package pl.sgorski.nethelt.webapi.features.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refresh-token")
public record RefreshTokenProperties(long expirationTimeInMs) {}
