package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl;

import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.OAuthUserInfo;

@Getter
public final class GoogleOAuth2UserInfo implements OAuthUserInfo {

  private final Map<String, Object> attributes;
  private final AuthProvider provider;
  private final String providerId;
  private final String email;

  public GoogleOAuth2UserInfo(Map<String, Object> attributes)
      throws InvalidOAuth2UserInfoException {
    this.attributes = attributes;
    this.provider = AuthProvider.GOOGLE;
    this.providerId =
        Optional.ofNullable(attributes.get("sub"))
            .orElseThrow(() -> new InvalidOAuth2UserInfoException("id"))
            .toString();
    this.email =
        Optional.ofNullable(attributes.get("email"))
            .orElseThrow(() -> new InvalidOAuth2UserInfoException("email"))
            .toString();
  }
}
