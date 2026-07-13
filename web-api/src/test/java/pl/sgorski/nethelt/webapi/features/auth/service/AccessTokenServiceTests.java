package pl.sgorski.nethelt.webapi.features.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.user.domain.Role;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.security.token.JwtPayload;
import pl.sgorski.nethelt.webapi.security.token.JwtService;

@ExtendWith(MockitoExtension.class)
public class AccessTokenServiceTests {

  @Mock private AuthProperties authProperties;
  @Mock private JwtService jwtService;
  @InjectMocks private AccessTokenService accessTokenService;

  @Test
  void generateAccessToken_shouldGenerateTokenWithCorrectPayload() {
    var user = mock(User.class);
    when(user.getEmail()).thenReturn("john.doe@example.com");
    doReturn(List.of(Role.USER)).when(user).getAuthorities();
    when(authProperties.jwtTokenExpiration()).thenReturn(Duration.ofMinutes(15));
    when(jwtService.generate(any(JwtPayload.class))).thenReturn("jwt-token");

    var result = accessTokenService.generateAccessToken(user);

    assertEquals("jwt-token", result);
    var captor = ArgumentCaptor.forClass(JwtPayload.class);
    verify(jwtService).generate(captor.capture());
    var payload = captor.getValue();

    assertNotNull(payload.subject());
    assertEquals(Duration.ofMinutes(15), payload.expiration());
    assertEquals("john.doe@example.com", payload.claims().get("email"));
    assertEquals(List.of("ROLE_USER"), payload.claims().get("roles"));
  }

  @Test
  void getEmailFromToken_shouldReturnEmailFromJwt() {
    var token = "jwt-token";
    when(jwtService.getClaim(token, "email", String.class)).thenReturn("test@test.com");

    var result = accessTokenService.getEmailFromToken(token);

    assertEquals("test@test.com", result);
    verify(jwtService).getClaim(token, "email", String.class);
  }

  @Test
  void isValid_shouldReturnTrue_whenTokenIsValid() {
    var token = "jwt-token";
    when(jwtService.isValid(token)).thenReturn(true);

    var result = accessTokenService.isValid(token);

    assertTrue(result);
    verify(jwtService).isValid(token);
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsInvalid() {
    var token = "jwt-token";
    when(jwtService.isValid(token)).thenReturn(false);

    var result = accessTokenService.isValid(token);

    assertFalse(result);
    verify(jwtService).isValid(token);
  }
}
