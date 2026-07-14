package pl.sgorski.nethelt.webapi.security.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.config.OAuth2Properties;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@ExtendWith(MockitoExtension.class)
public class OAuth2FailureHandlerTests {

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  @Mock private CookieService cookieService;
  @Mock private OAuth2Properties oAuth2Properties;
  @InjectMocks private OAuth2FailureHandler oAuth2FailureHandler;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void onAuthFailure_shouldOmit_whenNotRegisteredException() throws IOException {
    oAuth2FailureHandler.onAuthenticationFailure(request, response, new NotRegisteredException());

    verify(cookieService, never()).delete(anyString());
    assertNull(response.getRedirectedUrl());
  }

  @Test
  void onAuthFailure_shouldDeleteCookieAndSendRedirect_whenOAuth2AuthenticationException()
      throws IOException {
    when(oAuth2Properties.failureUrl()).thenReturn("/failure");

    oAuth2FailureHandler.onAuthenticationFailure(
        request, response, new OAuth2AuthenticationException("error-code"));

    verify(cookieService).delete(CookieNames.REFRESH_TOKEN);
    assertEquals("/failure?error=error-code", response.getRedirectedUrl());
  }

  private static class NotRegisteredException extends AuthenticationException {
    public NotRegisteredException() {
      super("Not registered exception");
    }
  }
}
