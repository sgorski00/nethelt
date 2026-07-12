package pl.sgorski.nethelt.webapi.features.auth.oauth2.connect;

import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;

public interface OAuth2ConnectService {
  boolean supports(OAuth2Mode mode);

  OAuth2User handle(OAuth2LoginContext context);
}
