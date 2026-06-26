package pl.sgorski.nethelt.webapi.security.session;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2Mode;

@Service
public final class OAuth2CookiesService {

    public boolean isLinkMode(String mode) {
        return OAuth2Mode.LINK.name().equalsIgnoreCase(mode);
    }

    public @Nullable Long getOAuthLinkUserId(String userId) {
        return userId == null ? null : Long.valueOf(userId);
    }
}

