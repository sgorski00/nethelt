package pl.sgorski.nethelt.webapi.security.authenticated.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.utils.TestOAuth2Factory;

@ExtendWith(MockitoExtension.class)
public class SecurityAuthenticatedUserResolverTests {

  @Mock private UserIdentityService userIdentityService;
  @InjectMocks private SecurityAuthenticatedUserResolver authenticatedUserResolver;

  @Test
  void requireUserId_shouldReturnUserId_whenLocalAccount() {
    var user = mockLocalUser(42L);
    var auth = new UsernamePasswordAuthenticationToken(user, "credentials");

    var result = authenticatedUserResolver.requireUserId(auth);

    assertEquals(42L, result);
    verify(userIdentityService, never()).findIdentity(any(), anyString());
  }

  @Test
  void requireUserId_shouldReturnUserId_whenOAuth2Account() {
    var user = TestOAuth2Factory.createGoogleOAuth2User("test-id", "john.doe@example.com");
    var auth = new OAuth2AuthenticationToken(user, null, "google");
    var localUser = mockLocalUser(1L);
    var identity = new UserIdentity(localUser, AuthProvider.GOOGLE, "test-id");
    when(userIdentityService.findIdentity(AuthProvider.GOOGLE, "test-id")).thenReturn(identity);

    var result = authenticatedUserResolver.requireUserId(auth);

    assertEquals(1L, result);
    verify(userIdentityService).findIdentity(AuthProvider.GOOGLE, "test-id");
  }

  @Test
  void requireUserId_shouldThrow_whenUnknownPrincipal() {
    var auth = new UsernamePasswordAuthenticationToken("not-valid-principal", "credentials");

    assertThrows(IllegalStateException.class, () -> authenticatedUserResolver.requireUserId(auth));
  }

  private User mockLocalUser(Long id) {
    var user = mock(User.class);
    when(user.getId()).thenReturn(id);
    return user;
  }
}
