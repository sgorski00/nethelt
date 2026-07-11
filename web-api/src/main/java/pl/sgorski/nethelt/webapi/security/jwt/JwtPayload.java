package pl.sgorski.nethelt.webapi.security.jwt;

import java.time.Duration;
import java.util.Map;

public record JwtPayload(String subject, Map<String, ?> claims, Duration expiration) {}
