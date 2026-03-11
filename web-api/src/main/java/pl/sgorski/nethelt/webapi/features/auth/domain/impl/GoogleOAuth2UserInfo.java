package pl.sgorski.nethelt.webapi.features.auth.domain.impl;

import lombok.Getter;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.domain.OAuthUserInfo;

import java.util.Map;

@Getter
public final class GoogleOAuth2UserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;
    private final AuthProvider authProvider;
    private final String providerId;
    private final String email;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.authProvider = AuthProvider.GOOGLE;
        this.providerId = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
    }
}
