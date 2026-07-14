package pl.sgorski.nethelt.webapi.web.cookie;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieNames {
  public static final String REFRESH_TOKEN = "refreshToken";
  public static final String OAUTH_CONTEXT = "oauth2_ctx";
  public static final String OAUTH_AUTH_REQUEST = "oauth2_auth_request";
}
