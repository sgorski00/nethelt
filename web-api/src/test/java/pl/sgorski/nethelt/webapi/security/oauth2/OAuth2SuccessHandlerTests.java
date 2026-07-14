package pl.sgorski.nethelt.webapi.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.domain.RefreshToken;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.config.OAuth2Properties;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.utils.TestOAuth2Factory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@ExtendWith(MockitoExtension.class)
public class OAuth2SuccessHandlerTests {

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  @Mock private CookieService cookieService;
  @Mock private AuthProperties authProperties;
  @Mock private OAuth2Properties oAuth2Properties;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private UserIdentityService userIdentityService;
  @InjectMocks private OAuth2SuccessHandler oAuth2SuccessHandler;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void onAuthSuccess_shouldSaveCookieAndSendRedirect_whenUserFound() throws IOException {
    var providerId = "provider-id";
    var auth = createTestAuthentication(providerId);
    var user = TestUserFactory.createOAuth2User(AuthProvider.GITHUB, providerId);
    var identity = user.getIdentities().iterator().next();
    when(userIdentityService.findIdentity(any(AuthProvider.class), anyString()))
        .thenReturn(identity);
    var refreshToken = new RefreshToken(user, Instant.MAX);
    when(refreshTokenService.generateRefreshToken(any(User.class))).thenReturn(refreshToken);
    when(authProperties.refreshTokenExpiration()).thenReturn(Duration.ofMinutes(60));
    when(oAuth2Properties.successUrl()).thenReturn("/success");

    oAuth2SuccessHandler.onAuthenticationSuccess(request, response, auth);

    assertEquals("/success", response.getRedirectedUrl());
    verify(cookieService)
        .save(
            eq(CookieNames.REFRESH_TOKEN), eq(refreshToken.getToken()), eq(Duration.ofMinutes(60)));
    verify(refreshTokenService).generateRefreshToken(same(user));
    verify(userIdentityService).findIdentity(AuthProvider.GITHUB, identity.getProviderId());
  }

  @Test
  void onAuthSuccess_shouldSendError_whenUserNotFound() throws IOException {
    var auth = createTestAuthentication("provider-id");
    var identity = new UserIdentity(null, AuthProvider.GITHUB, "test-id");
    when(userIdentityService.findIdentity(any(AuthProvider.class), anyString()))
        .thenReturn(identity);

    oAuth2SuccessHandler.onAuthenticationSuccess(request, response, auth);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    assertNotNull(response.getErrorMessage());
    verify(cookieService, never()).save(any(), anyString(), any());
  }

  private OAuth2AuthenticationToken createTestAuthentication(String providerId) {
    var oAuth2User = TestOAuth2Factory.createGithubOAuth2User(providerId, "john.doe@example.com");
    return new OAuth2AuthenticationToken(oAuth2User, null, "github");
  }
}
