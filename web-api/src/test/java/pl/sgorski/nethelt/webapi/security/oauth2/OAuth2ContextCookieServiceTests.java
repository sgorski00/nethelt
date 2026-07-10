package pl.sgorski.nethelt.webapi.security.oauth2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@ExtendWith(MockitoExtension.class)
public class OAuth2ContextCookieServiceTests {

  @Mock private CookieService cookieService;

  @InjectMocks private OAuth2ContextCookieService oAuth2ContextCookieService;

  @Test
  void clearContext_shouldAddClearCookie() {
    oAuth2ContextCookieService.clearContext();
  }
}
