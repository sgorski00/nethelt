package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.repository.UserIdentityRepository;

@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;

    public boolean isUserIdentityPresent(String providerId, AuthProvider authProvider) {
        return userIdentityRepository.existsByProviderAndProviderId(authProvider, providerId);
    }
}
