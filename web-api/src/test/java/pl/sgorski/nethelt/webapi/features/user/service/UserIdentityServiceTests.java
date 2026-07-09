package pl.sgorski.nethelt.webapi.features.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.IdentityNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;
import pl.sgorski.nethelt.webapi.features.user.repository.UserIdentityRepository;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class UserIdentityServiceTests {

  private static final AuthProvider PROVIDER = AuthProvider.GITHUB;
  private static final String PROVIDER_ID = "provider-id";

  @Mock private UserIdentityRepository userIdentityRepository;

  @InjectMocks private UserIdentityService userIdentityService;

  @Test
  void isUserIdentityPresent_shouldReturnTrue_whenIsPresent() {
    when(userIdentityRepository.existsByProviderAndProviderId(PROVIDER, PROVIDER_ID))
        .thenReturn(true);

    var result = userIdentityService.isUserIdentityPresent(PROVIDER_ID, PROVIDER);

    assertTrue(result);
  }

  @Test
  void isUserIdentityPresent_shouldReturnFalse_whenIsNotPresent() {
    when(userIdentityRepository.existsByProviderAndProviderId(PROVIDER, PROVIDER_ID))
        .thenReturn(false);

    var result = userIdentityService.isUserIdentityPresent(PROVIDER_ID, PROVIDER);

    assertFalse(result);
  }

  @Test
  void findIdentity_shouldReturnCorrectIdentity() {
    var user = TestUserFactory.createLocalUser();
    var identity = new UserIdentity(user, PROVIDER, PROVIDER_ID);
    when(userIdentityRepository.findWithUserByProviderAndProviderId(PROVIDER, PROVIDER_ID))
        .thenReturn(Optional.of(identity));

    var result = userIdentityService.findIdentity(PROVIDER, PROVIDER_ID);

    assertSame(identity, result);
  }

  @Test
  void findIdentity_shouldThrow_whenIdentityNotFound() {
    when(userIdentityRepository.findWithUserByProviderAndProviderId(PROVIDER, PROVIDER_ID))
        .thenReturn(Optional.empty());

    assertThrows(
        IdentityNotFoundException.class,
        () -> userIdentityService.findIdentity(PROVIDER, PROVIDER_ID));
  }
}
