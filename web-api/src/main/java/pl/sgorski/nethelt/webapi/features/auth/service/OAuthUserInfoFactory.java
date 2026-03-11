package pl.sgorski.nethelt.webapi.features.auth.service;

import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.domain.OAuthUserInfo;
import pl.sgorski.nethelt.webapi.features.auth.domain.impl.GoogleOAuth2UserInfo;

import java.util.Map;

public final class OAuthUserInfoFactory {

    public static OAuthUserInfo create(AuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
        };
    }
}
