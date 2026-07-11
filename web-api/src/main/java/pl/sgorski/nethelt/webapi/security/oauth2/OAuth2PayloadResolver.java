package pl.sgorski.nethelt.webapi.security.oauth2;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Service
@RequiredArgsConstructor
public final class OAuth2PayloadResolver {

  private final CookieService cookieService;
  private final OAuth2ContextService contextService;

  public Optional<OAuth2ContextPayload> consume() {
    var token = cookieService.find(CookieNames.OAUTH_CONTEXT);
    try {
      return token.map(contextService::parse);
    } finally {
      cookieService.delete(CookieNames.OAUTH_CONTEXT);
    }
  }
}
