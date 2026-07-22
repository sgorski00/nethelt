package pl.sgorski.nethelt.webapi.features.network.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.network.repository.NetworkRepository;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@Component
@RequiredArgsConstructor
public class NetworkAuthorization {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final NetworkRepository networkRepository;

  public boolean isOwner(Authentication authentication, Long networkId) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    return networkRepository.existsByIdAndUserId(networkId, userId);
  }
}
