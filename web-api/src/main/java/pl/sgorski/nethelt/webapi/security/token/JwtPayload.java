package pl.sgorski.nethelt.webapi.security.token;

import java.time.Duration;
import java.util.Map;

public record JwtPayload(String subject, Map<String, ?> claims, Duration expiration) {}
