package pl.sgorski.nethelt.webapi.security.service;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth.factory.OAuth2UserInfoFactory;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2AccountLinkService;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2CommonLoginService;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2GithubEmailService;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2Mode;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2PayloadResolver;

@Log4j2
@Service
@RequiredArgsConstructor
public final class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

  private final OAuth2CommonLoginService oAuth2CommonLoginService;
  private final OAuth2AccountLinkService oAuth2AccountLinkService;
  private final OAuth2PayloadResolver oAuth2PayloadResolver;
  private final OAuth2GithubEmailService oAuth2GithubEmailService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    log.debug("Loading user from OAuth2 provider");
    var oauthUser = super.loadUser(userRequest);
    var providerStr = userRequest.getClientRegistration().getRegistrationId();
    var provider = AuthProvider.fromString(providerStr);
    if (provider.equals(AuthProvider.GITHUB)) {
      var accessToken = userRequest.getAccessToken().getTokenValue();
      var githubEmail = oAuth2GithubEmailService.getGithubAccountEmail(accessToken);
      var modifiedAttributes = new HashMap<>(oauthUser.getAttributes());
      modifiedAttributes.put("email", githubEmail);
      oauthUser = new DefaultOAuth2User(oauthUser.getAuthorities(), modifiedAttributes, "id");
    }

    var payload = oAuth2PayloadResolver.consume().orElse(null);
    var linkMode = payload != null && payload.mode() == OAuth2Mode.LINK;
    var userId = payload != null ? payload.userId() : null;
    var context =
        new OAuth2LoginContext(
            oauthUser,
            provider,
            OAuth2UserInfoFactory.create(provider, oauthUser.getAttributes()),
            linkMode,
            userId);

    log.debug(
        "Processing user {} from OAuth2 provider: {}", context.userInfo().getEmail(), providerStr);
    if (context.linkMode()) {
      return oAuth2AccountLinkService.handle(context);
    }
    return oAuth2CommonLoginService.handle(context);
  }
}
