package pl.sgorski.nethelt.webapi.features.auth.oauth2;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record OAuth2LoginContext(
    OAuth2User oauthUser,
    AuthProvider provider,
    OAuthUserInfo userInfo,
    boolean linkMode,
    @Nullable Long linkUserId) {}
