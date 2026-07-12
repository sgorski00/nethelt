package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl;

import java.util.Map;
import lombok.Getter;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.OAuthUserInfo;

@Getter
public final class GoogleOAuth2UserInfo implements OAuthUserInfo {

  private final Map<String, Object> attributes;
  private final AuthProvider provider;
  private final String providerId;
  private final String email;

  public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
    this.provider = AuthProvider.GOOGLE;
    this.providerId = (String) attributes.get("sub");
    this.email = (String) attributes.get("email");
  }
}
