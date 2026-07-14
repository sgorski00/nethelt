package pl.sgorski.nethelt.webapi.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountAlreadyLinkedException;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountLinkRequiredException;
import pl.sgorski.nethelt.webapi.exception.oauth2.IncompleteOAuth2DataException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.addon.OAuth2GithubEmailService;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.connect.OAuth2ConnectService;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2ContextPayload;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2PayloadResolver;
import pl.sgorski.nethelt.webapi.utils.TestOAuth2Factory;

@ExtendWith(MockitoExtension.class)
public class OAuth2UserServiceImplTests {

  @Mock private OAuth2ConnectService connectService;
  @Mock private OAuth2PayloadResolver payloadResolver;
  @Mock private OAuth2GithubEmailService githubEmailService;
  @Mock private OAuth2UserRequest userRequest;

  private OAuth2UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    this.userService =
        spy(
            new OAuth2UserServiceImpl(
                List.of(connectService), payloadResolver, githubEmailService));
  }

  @Test
  void loadUser_shouldOverrideWithGithubUser_whenProviderIsGithub() {
    mockClientRegistration("github");
    var user = mockGithubUser();
    var tokenValue = "token";
    var at = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, tokenValue, null, null);
    when(userRequest.getAccessToken()).thenReturn(at);
    when(githubEmailService.getGithubAccountEmail(tokenValue)).thenReturn("john.doe@example.com");
    when(payloadResolver.consume()).thenReturn(Optional.empty());
    when(connectService.supports(any())).thenReturn(true);
    when(connectService.handle(any())).thenReturn(user);

    assertDoesNotThrow(() -> userService.loadUser(userRequest));
    verify(githubEmailService).getGithubAccountEmail(tokenValue);
    verify(connectService)
        .handle(argThat(context -> context.userInfo().getEmail().equals("john.doe@example.com")));
  }

  @Test
  void loadUser_shouldUseCommonLoginService_whenLoginModeIsEnabled() {
    mockClientRegistration("google");
    var user = mockGoogleUser();
    mockContextPayload(OAuth2Mode.LOGIN);
    when(connectService.handle(any())).thenReturn(user);
    when(connectService.supports(OAuth2Mode.LOGIN)).thenReturn(true);

    assertDoesNotThrow(() -> userService.loadUser(userRequest));
    verify(connectService, times(1)).handle(any());
  }

  @Test
  void loadUser_shouldUseAccountLinkService_whenLinkModeIsEnabled() {
    mockClientRegistration("google");
    var user = mockGoogleUser();
    mockContextPayload(OAuth2Mode.LINK);
    when(connectService.supports(OAuth2Mode.LINK)).thenReturn(true);
    when(connectService.handle(any())).thenReturn(user);

    assertDoesNotThrow(() -> userService.loadUser(userRequest));
    verify(connectService, times(1)).handle(any());
  }

  @Test
  void loadUser_shouldThrowOAuth2Exception_whenAccountLinkIsRequired() {
    mockClientRegistration("google");
    mockGoogleUser();
    mockContextPayload(OAuth2Mode.LOGIN);
    when(connectService.supports(any())).thenReturn(true);
    when(connectService.handle(any())).thenThrow(new AccountLinkRequiredException());

    var thrown =
        assertThrows(OAuth2AuthenticationException.class, () -> userService.loadUser(userRequest));
    assertEquals("account-link-required", thrown.getError().getErrorCode());
  }

  @Test
  void loadUser_shouldThrowOAuth2Exception_whenAccountIsAlreadyLinked() {
    mockClientRegistration("google");
    mockGoogleUser();
    mockContextPayload(OAuth2Mode.LINK);
    when(connectService.supports(any())).thenReturn(true);
    when(connectService.handle(any())).thenThrow(new AccountAlreadyLinkedException());

    var thrown =
        assertThrows(OAuth2AuthenticationException.class, () -> userService.loadUser(userRequest));
    assertEquals("account-already-linked", thrown.getError().getErrorCode());
  }

  @Test
  void loadUser_shouldThrowOAuth2Exception_whenOAuthDataIsIncomplete() {
    mockClientRegistration("google");
    mockGoogleUser();
    mockContextPayload(OAuth2Mode.LOGIN);
    when(connectService.supports(any())).thenReturn(true);
    when(connectService.handle(any())).thenThrow(new IncompleteOAuth2DataException());

    var thrown =
        assertThrows(OAuth2AuthenticationException.class, () -> userService.loadUser(userRequest));
    assertEquals("oauth2-incomplete-data", thrown.getError().getErrorCode());
  }

  @Test
  void loadUser_shouldThrowOAuth2Exception_whenNoSuitableServiceFound() {
    mockClientRegistration("google");
    mockGoogleUser();
    mockContextPayload(OAuth2Mode.LOGIN);
    when(connectService.supports(any())).thenReturn(false);

    var thrown =
        assertThrows(OAuth2AuthenticationException.class, () -> userService.loadUser(userRequest));
    assertEquals("oauth2-incomplete-data", thrown.getError().getErrorCode());
  }

  @Test
  void loadUser_shouldThrowOAuth2Exception_whenInvalidOAuth2UserInfoPassed() {
    mockClientRegistration("google");
    mockInvalidUser();
    mockContextPayload(OAuth2Mode.LOGIN);

    var thrown =
        assertThrows(OAuth2AuthenticationException.class, () -> userService.loadUser(userRequest));
    assertEquals("invalid-oauth2-user-info", thrown.getError().getErrorCode());
  }

  private void mockClientRegistration(String provider) {
    var cr =
        ClientRegistration.withRegistrationId(provider)
            .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
            .build();
    when(userRequest.getClientRegistration()).thenReturn(cr);
  }

  private void mockContextPayload(OAuth2Mode login) {
    var ctxPayload = new OAuth2ContextPayload(1L, login);
    when(payloadResolver.consume()).thenReturn(Optional.of(ctxPayload));
  }

  private OAuth2User mockGithubUser() {
    var user = TestOAuth2Factory.createGithubOAuth2User("john.doe@example.com");
    doReturn(user).when(userService).loadOAuth2User(any());
    return user;
  }

  private OAuth2User mockGoogleUser() {
    var user = TestOAuth2Factory.createGoogleOAuth2User("john.doe@example.com");
    doReturn(user).when(userService).loadOAuth2User(any());
    return user;
  }

  private void mockInvalidUser() {
    var user =
        new DefaultOAuth2User(
            null,
            new HashMap<>() {
              {
                put("sub", "test-id");
                put("email", null);
              }
            },
            "sub");
    doReturn(user).when(userService).loadOAuth2User(any());
  }
}
