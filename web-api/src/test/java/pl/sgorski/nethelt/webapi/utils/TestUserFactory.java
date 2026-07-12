package pl.sgorski.nethelt.webapi.utils;

import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

public final class TestUserFactory {

  public static User createOAuth2User(AuthProvider provider) {
    return new User("john.doe@example.com", provider, "provider-id");
  }

  public static User createLocalUser() {
    return createLocalUser("john.doe@example.com");
  }

  public static User createLocalUser(String email) {
    return new User(email, "hashed_password");
  }
}
