package pl.sgorski.nethelt.webapi.features.auth.oauth2.connect.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountAlreadyLinkedException;
import pl.sgorski.nethelt.webapi.exception.oauth2.IncompleteOAuth2DataException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GoogleOAuth2UserInfo;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class OAuth2AccountLinkServiceTests {

  @Mock private UserService userService;
  @Mock private UserIdentityService userIdentityService;
  @InjectMocks private OAuth2AccountLinkService connectService;

  private OAuth2User oAuthUser;

  @BeforeEach
  void setUp() {
    this.oAuthUser = new DefaultOAuth2User(null, Map.of("email", "john.doe@example.com"), "email");
  }

  @Test
  void supports_shouldReturnTrue_whenModeIsLink() {
    var mode = OAuth2Mode.LINK;

    var result = connectService.supports(mode);

    assertTrue(result);
  }

  @Test
  void supports_shouldReturnFalse_whenModeIsNotLink() {
    var mode = OAuth2Mode.LOGIN;

    var result = connectService.supports(mode);

    assertFalse(result);
  }

  @Test
  void handle_shouldThrowIncompleteOAuth2DataException_whenUserIdNotPassed() {
    var ctx = createContext(null);

    assertThrows(IncompleteOAuth2DataException.class, () -> connectService.handle(ctx));
  }

  @Test
  void handle_shouldThrowAccountAlreadyLinkedException_whenIdentityIsAlreadyTaken() {
    var ctx = createContext(1L);
    when(userIdentityService.isUserIdentityPresent(nullable(String.class), any(AuthProvider.class)))
        .thenReturn(true);

    assertThrows(AccountAlreadyLinkedException.class, () -> connectService.handle(ctx));
  }

  @Test
  void handle_shouldAddIdentityToExistingUser_whenRequestIsValid() {
    var user = TestUserFactory.createLocalUser();
    var ctx = createContext(1L);
    when(userIdentityService.isUserIdentityPresent(nullable(String.class), any(AuthProvider.class)))
        .thenReturn(false);
    when(userService.getUserWithProfileAndIdentities(1L)).thenReturn(user);

    var result = connectService.handle(ctx);

    verify(userService).save(user);
    assertTrue(user.hasIdentity(ctx.provider()));
    assertSame(oAuthUser, result);
  }

  private OAuth2LoginContext createContext(@Nullable Long userId) {
    var userInfo = new GoogleOAuth2UserInfo(Map.of());
    return new OAuth2LoginContext(oAuthUser, AuthProvider.GOOGLE, userInfo, userId);
  }
}
