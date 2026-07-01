package pl.sgorski.nethelt.webapi.features.auth.service;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth.OAuthUserInfo;

public record OAuth2LoginContext(
    OAuth2User oauthUser,
    AuthProvider provider,
    OAuthUserInfo userInfo,
    boolean linkMode,
    @Nullable Long linkUserId) {}
