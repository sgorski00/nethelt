package pl.sgorski.nethelt.webapi.features.auth.domain.impl;

import lombok.Getter;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.domain.OAuthUserInfo;

import java.util.Map;

@Getter
public final class GithubOAuth2UserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;
    private final AuthProvider provider;
    private final String providerId;
    private final String email;

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.provider = AuthProvider.GITHUB;
        this.providerId = String.valueOf(attributes.get("id"));
        this.email = (String) attributes.get("email");
    }
}
