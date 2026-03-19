package pl.sgorski.nethelt.webapi.security.authenticated;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;

@Component
@RequiredArgsConstructor
public final class SecurityAuthenticatedUserResolver implements AuthenticatedUserResolver {

    private final UserIdentityService identityService;

    @Override
    public Long requireUserId(Authentication authentication) {
        var principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            var providerId = oauthToken.getName();
            var provider = AuthProvider.fromString(oauthToken.getAuthorizedClientRegistrationId());
            var identity = identityService.findIdentity(provider, providerId);
            return identity.getUser().getId();
        }

        throw new IllegalStateException("Unsupported authentication principal");
    }
}

