package pl.sgorski.nethelt.webapi.features.auth.oauth2.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum OAuth2Mode {
  LOGIN,
  LINK;

  @JsonCreator
  public static OAuth2Mode fromString(String value) {
    return Arrays.stream(values())
        .filter(mode -> mode.toString().equalsIgnoreCase(value.trim()))
        .findFirst()
        .orElseThrow();
  }
}
