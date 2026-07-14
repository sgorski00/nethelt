package pl.sgorski.nethelt.webapi.features.auth.oauth2.context;

import org.jspecify.annotations.Nullable;

public record OAuth2ContextPayload(@Nullable Long userId, OAuth2Mode mode) {
  public static OAuth2ContextPayload withDefaults() {
    return new OAuth2ContextPayload(null, OAuth2Mode.LOGIN);
  }
}
