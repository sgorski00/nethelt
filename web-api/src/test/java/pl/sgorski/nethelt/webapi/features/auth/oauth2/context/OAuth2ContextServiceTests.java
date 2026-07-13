package pl.sgorski.nethelt.webapi.features.auth.oauth2.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.security.token.JwtPayload;
import pl.sgorski.nethelt.webapi.security.token.JwtService;

@ExtendWith(MockitoExtension.class)
public class OAuth2ContextServiceTests {

  @Mock private JwtService jwtService;
  @InjectMocks private OAuth2ContextService contextService;

  @ParameterizedTest
  @EnumSource(OAuth2Mode.class)
  void generateContextToken_shouldGenerateValidContext(OAuth2Mode mode) {
    when(jwtService.generate(any(JwtPayload.class))).thenReturn("mocked-token");
    var duration = Duration.ofSeconds(60);

    var result = contextService.generateContextToken(1L, mode, duration);

    var expectedPayload = new JwtPayload("1", Map.of("mode", mode.name()), duration);
    verify(jwtService).generate(expectedPayload);
    assertEquals("mocked-token", result);
  }

  @Test
  void parse_shouldParseTokenWithCorrectValues() {
    var token = "test-token";
    when(jwtService.getSubject(token)).thenReturn("1");
    when(jwtService.getClaim(token, "mode", String.class)).thenReturn("LOGIN");

    var result = contextService.parse(token);

    var expected = new OAuth2ContextPayload(1L, OAuth2Mode.LOGIN);
    assertEquals(expected, result);
  }
}
