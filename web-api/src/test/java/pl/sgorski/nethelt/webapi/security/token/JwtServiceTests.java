package pl.sgorski.nethelt.webapi.security.token;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtServiceTests {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    var secretKey = Jwts.SIG.HS256.key().build();
    this.jwtService = new JwtService(secretKey);
  }

  @Test
  void generate_shouldGenerateCorrectJwtToken_whenClaimsAreEmpty() {
    var result = generateTokenWithClaims(Map.of());

    assertNotNull(result);
  }

  @Test
  void generate_shouldIncludeSubjectAndClaims_whenClaimsAreNotEmpty() {
    var token = generateTokenWithClaims(Map.of("role", "ADMIN"));

    assertEquals("test-subject", jwtService.getSubject(token));
    assertEquals("ADMIN", jwtService.getClaim(token, "role", String.class));
  }

  @Test
  void isValid_shouldReturnTrue_whenTokenIsValid() {
    var token = generateTokenWithClaims(Map.of());

    var result = jwtService.isValid(token);

    assertTrue(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenHasExpired() {
    var payload = new JwtPayload("test-subject", Map.of(), Duration.ofSeconds(-1));
    var token = jwtService.generate(payload);

    var result = jwtService.isValid(token);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsNotValid() {
    var token = "definetly-not-a-valid-token";

    var result = jwtService.isValid(token);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenSecretKeyHasChanged() {
    var token = generateTokenWithClaims(Map.of());
    var otherKey = Jwts.SIG.HS256.key().build();
    var otherJwtService = new JwtService(otherKey);

    var result = otherJwtService.isValid(token);

    assertFalse(result);
  }

  @Test
  void getClaim_shouldReturnClaim() {
    var token = generateTokenWithClaims(Map.of("userId", 15L, "role", "ADMIN"));

    assertEquals(15L, jwtService.getClaim(token, "userId", Long.class));
    assertEquals("ADMIN", jwtService.getClaim(token, "role", String.class));
  }

  @Test
  void getSubject_shouldReturnSubject() {
    var token = generateTokenWithClaims(Map.of());

    assertEquals("test-subject", jwtService.getSubject(token));
  }

  private String generateTokenWithClaims(Map<String, Object> claims) {
    var payload = new JwtPayload("test-subject", claims, Duration.ofMinutes(5));
    return jwtService.generate(payload);
  }
}
