package pl.sgorski.nethelt.webapi.features.auth.oauth2.factory;

import java.util.Map;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.OAuthUserInfo;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.provider.GithubOAuth2UserInfo;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.provider.GoogleOAuth2UserInfo;

public final class OAuth2UserInfoFactory {

  public static OAuthUserInfo create(AuthProvider provider, Map<String, Object> attributes) {
    return switch (provider) {
      case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
      case GITHUB -> new GithubOAuth2UserInfo(attributes);
    };
  }
}
