package pl.sgorski.nethelt.webapi.security.oauth2;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class OAuth2PayloadResolver {

  private final OAuth2ContextCookieService cookieService;
  private final OAuth2ContextService contextService;

  public Optional<OAuth2ContextPayload> consume() {
    var token = cookieService.readOauthContextFromCookies();
    try {
      return token.map(contextService::parse);
    } finally {
      cookieService.clearContext();
    }
  }
}
