package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.domain.IdentityNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;
import pl.sgorski.nethelt.webapi.features.user.repository.UserIdentityRepository;

@Service
@RequiredArgsConstructor
public class UserIdentityService {

  private final UserIdentityRepository userIdentityRepository;

  public boolean isUserIdentityPresent(String providerId, AuthProvider authProvider) {
    return userIdentityRepository.existsByProviderAndProviderId(authProvider, providerId);
  }

  public UserIdentity findIdentity(AuthProvider provider, String providerId) {
    return userIdentityRepository
        .findWithUserByProviderAndProviderId(provider, providerId)
        .orElseThrow(
            () ->
                new IdentityNotFoundException(
                    "User identity not found for provider: "
                        + provider.name()
                        + ", providerId: "
                        + providerId));
  }
}
