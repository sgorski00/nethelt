package pl.sgorski.nethelt.webapi.features.auth.oauth2.connect.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountLinkRequiredException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GoogleOAuth2UserInfo;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
public class OAuth2CommonLoginServiceTests {

  @Mock private UserService userService;
  @Mock private UserIdentityService userIdentityService;
  @InjectMocks private OAuth2CommonLoginService connectService;

  private OAuth2User oAuthUser;
  private OAuth2LoginContext ctx;

  @BeforeEach
  void setUp() {
    this.oAuthUser = new DefaultOAuth2User(null, Map.of("email", "john.doe@example.com"), "email");
    var userInfo = new GoogleOAuth2UserInfo(Map.of());
    this.ctx = new OAuth2LoginContext(oAuthUser, AuthProvider.GOOGLE, userInfo, null);
  }

  @Test
  void supports_shouldReturnTrue_whenModeIsLogin() {
    var mode = OAuth2Mode.LOGIN;

    var result = connectService.supports(mode);

    assertTrue(result);
  }

  @Test
  void supports_shouldReturnFalse_whenModeIsNotLogin() {
    var mode = OAuth2Mode.LINK;

    var result = connectService.supports(mode);

    assertFalse(result);
  }

  @Test
  void handle_shouldLogin_whenIdentityIsPresent() {
    when(userIdentityService.isUserIdentityPresent(nullable(String.class), any(AuthProvider.class)))
        .thenReturn(true);

    var result = connectService.handle(ctx);

    assertSame(oAuthUser, result);
  }

  @Test
  void handle_shouldThrowAccountLinkRequireException_whenAccountWithSameEmailExists() {
    when(userIdentityService.isUserIdentityPresent(nullable(String.class), any(AuthProvider.class)))
        .thenReturn(false);
    when(userService.isUserPresent(nullable(String.class))).thenReturn(true);

    assertThrows(AccountLinkRequiredException.class, () -> connectService.handle(ctx));
  }

  @Test
  void handle_shouldCreateNewAccount_whenIdentityAndExistingUserNotFound() {
    when(userIdentityService.isUserIdentityPresent(nullable(String.class), any(AuthProvider.class)))
        .thenReturn(false);
    when(userService.isUserPresent(nullable(String.class))).thenReturn(false);

    var result = connectService.handle(ctx);

    verify(userService).save(argThat(user -> user.hasIdentity(ctx.provider())));
    assertSame(oAuthUser, result);
  }
}
