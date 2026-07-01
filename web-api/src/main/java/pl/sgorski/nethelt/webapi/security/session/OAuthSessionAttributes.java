package pl.sgorski.nethelt.webapi.security.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OAuthSessionAttributes {
  OAUTH_MODE("oauth2_mode"),
  OAUTH_LINK_USER_ID("oauth2_link_user_id");

  private final String attributeName;
}
