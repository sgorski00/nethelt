package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GithubOAuth2UserInfo;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl.GoogleOAuth2UserInfo;

public class OAuth2UserInforFactoryTests {

  @Test
  void create_shouldCreateCorrectUserInfo_whenProviderIsGoogle() {
    var result =
        OAuth2UserInfoFactory.create(
            AuthProvider.GOOGLE,
            Map.of(
                "sub", "1234567890",
                "email", "joh.doe@exmaple.com"));

    assertNotNull(result);
    assertInstanceOf(GoogleOAuth2UserInfo.class, result);
  }

  @Test
  void create_shouldCreateCorrectUserInfo_whenProviderIsGithub() {
    var result =
        OAuth2UserInfoFactory.create(
            AuthProvider.GITHUB,
            Map.of(
                "id", "1234567890",
                "email", "joh.doe@exmaple.com"));

    assertNotNull(result);
    assertInstanceOf(GithubOAuth2UserInfo.class, result);
  }

  @Test
  void create_shouldThrow_whenErrorThrownInUserInfoConstructor() {
    assertThrows(
        InvalidOAuth2UserInfoException.class,
        () -> OAuth2UserInfoFactory.create(AuthProvider.GITHUB, Map.of()));
  }
}
