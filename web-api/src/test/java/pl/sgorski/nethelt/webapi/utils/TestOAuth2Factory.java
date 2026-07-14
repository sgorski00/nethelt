package pl.sgorski.nethelt.webapi.utils;

import java.util.Map;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.OAuthUserInfo;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GoogleOAuth2UserInfo;

public class TestOAuth2Factory {

  public static OAuthUserInfo createGoogleOAuth2UserInfo(String email) {
    return new GoogleOAuth2UserInfo(Map.of("sub", "test-provider-id", "email", email));
  }

  public static OAuth2User createGoogleOAuth2User(String email) {
    return new DefaultOAuth2User(null, Map.of("sub", "test-id", "email", email), "sub");
  }

  public static OAuth2User createGoogleOAuth2User(String id, String email) {
    return new DefaultOAuth2User(null, Map.of("sub", id, "email", email), "sub");
  }

  public static OAuth2User createGithubOAuth2User(String email) {
    return new DefaultOAuth2User(null, Map.of("id", "test-id", "email", email), "id");
  }

  public static OAuth2User createGithubOAuth2User(String id, String email) {
    return new DefaultOAuth2User(null, Map.of("id", id, "email", email), "id");
  }

  public static OAuth2LoginContext createOAuth2LoginContext(
      OAuth2User user, AuthProvider authProvider, OAuthUserInfo userInfo, @Nullable Long userId) {
    return new OAuth2LoginContext(user, authProvider, userInfo, userId);
  }
}
