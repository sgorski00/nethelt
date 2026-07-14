package pl.sgorski.nethelt.webapi.features.auth.oauth2.context;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@ExtendWith(MockitoExtension.class)
public class OAuth2PayloadResolverTests {

  @Mock private CookieService cookieService;
  @Mock private OAuth2ContextService contextService;
  @InjectMocks private OAuth2PayloadResolver payloadResolver;

  @Test
  void consume_shouldReturnParsedPayload_whenTokenFound() {
    var payload = new OAuth2ContextPayload(1L, OAuth2Mode.LOGIN);
    when(cookieService.find(CookieNames.OAUTH_CONTEXT)).thenReturn(Optional.of("mocked-token"));
    when(contextService.parse("mocked-token")).thenReturn(payload);

    var result = payloadResolver.consume();

    assertTrue(result.isPresent());
    assertSame(payload, result.get());
    verify(cookieService).delete(CookieNames.OAUTH_CONTEXT);
  }

  @Test
  void consume_shouldOptionalEmpty_whenTokenNotFound() {
    when(cookieService.find(CookieNames.OAUTH_CONTEXT)).thenReturn(Optional.empty());

    var result = payloadResolver.consume();

    assertTrue(result.isEmpty());
    verify(cookieService).delete(CookieNames.OAUTH_CONTEXT);
  }
}
