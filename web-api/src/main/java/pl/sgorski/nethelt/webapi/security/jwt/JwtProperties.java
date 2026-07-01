package pl.sgorski.nethelt.webapi.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secretKey, Long expirationTimeInMs) {}
