package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.factory;

import java.util.Map;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.OAuthUserInfo;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GithubOAuth2UserInfo;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GoogleOAuth2UserInfo;

public final class OAuth2UserInfoFactory {

  public static OAuthUserInfo create(AuthProvider provider, Map<String, Object> attributes) {
    return switch (provider) {
      case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
      case GITHUB -> new GithubOAuth2UserInfo(attributes);
    };
  }
}
