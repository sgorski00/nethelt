package pl.sgorski.nethelt.webapi.security.oauth2;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.security.jwt.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public final class OAuth2ContextService {

    private static final Duration OAUTH2_CONTEXT_EXPIRATION = Duration.ofMinutes(5);

    private final SecretKey secretKey;

    public String generate(Long userId, OAuth2Mode mode) {
        var now = Instant.now();
        var expirationTime = now.plus(OAUTH2_CONTEXT_EXPIRATION);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("mode", mode.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(this.secretKey)
                .compact();
    }

    public OAuth2ContextPayload parse(String token) {
        var claims = getClaimsFromToken(token);
        var userId = Long.valueOf(claims.getSubject());
        var mode = OAuth2Mode.fromString(claims.get("mode", String.class));
        return new OAuth2ContextPayload(userId, mode);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

