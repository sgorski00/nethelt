package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;

public class GithubOAuth2UserInfoTests {

  @Test
  void constructor_shouldCreateCorrectUserInfo() {
    var attr = createAttributes("test-provider-id", "john.doe@example.com");

    var result = new GithubOAuth2UserInfo(attr);

    assertEquals(AuthProvider.GITHUB, result.getProvider());
    assertEquals("test-provider-id", result.getProviderId());
    assertEquals("john.doe@example.com", result.getEmail());
    assertSame(attr, result.getAttributes());
  }

  @Test
  void constructor_shouldThrow_whenEmailIsNull() {
    var attr = createAttributes("test-provider-id", null);

    assertThrows(InvalidOAuth2UserInfoException.class, () -> new GithubOAuth2UserInfo(attr));
  }

  @Test
  void constructor_shouldThrow_whenIdIsNull() {
    var attr = createAttributes(null, "john.doe@example.com");

    assertThrows(InvalidOAuth2UserInfoException.class, () -> new GithubOAuth2UserInfo(attr));
  }

  private Map<String, Object> createAttributes(String id, String email) {
    return new HashMap<>() {
      {
        put("id", id);
        put("email", email);
        put("additional", "value");
      }
    };
  }
}
