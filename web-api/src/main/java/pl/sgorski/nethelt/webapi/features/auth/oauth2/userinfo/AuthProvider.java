package pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import pl.sgorski.nethelt.webapi.exception.domain.ProviderNotFoundException;

public enum AuthProvider {
  GOOGLE,
  GITHUB;

  @JsonCreator
  public static AuthProvider fromString(String value) {
    return Arrays.stream(values())
        .filter(provider -> provider.name().equalsIgnoreCase(value.trim()))
        .findFirst()
        .orElseThrow(() -> new ProviderNotFoundException("Invalid provider: " + value));
  }
}
