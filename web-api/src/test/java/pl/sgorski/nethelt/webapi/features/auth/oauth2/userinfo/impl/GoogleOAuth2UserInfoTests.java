package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;

public class GoogleOAuth2UserInfoTests {

  @Test
  void constructor_shouldCreateCorrectUserInfo() {
    var attr = createAttributes("test-provider-id", "john.doe@example.com");

    var result = new GoogleOAuth2UserInfo(attr);

    assertEquals(AuthProvider.GOOGLE, result.getProvider());
    assertEquals("test-provider-id", result.getProviderId());
    assertEquals("john.doe@example.com", result.getEmail());
    assertSame(attr, result.getAttributes());
  }

  @Test
  void constructor_shouldThrow_whenEmailIsNull() {
    var attr = createAttributes("test-provider-id", null);

    assertThrows(InvalidOAuth2UserInfoException.class, () -> new GoogleOAuth2UserInfo(attr));
  }

  @Test
  void constructor_shouldThrow_whenIdIsNull() {
    var attr = createAttributes(null, "john.doe@example.com");

    assertThrows(InvalidOAuth2UserInfoException.class, () -> new GoogleOAuth2UserInfo(attr));
  }

  private Map<String, Object> createAttributes(String id, String email) {
    return new HashMap<>() {
      {
        put("sub", id);
        put("email", email);
        put("additional", "value");
      }
    };
  }
}
