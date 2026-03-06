package pl.sgorski.nethelt.webapi.features.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secretKey,
        Long expirationTimeInMs
) { }
