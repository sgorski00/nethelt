package pl.sgorski.nethelt.webapi.security.service;

import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountAlreadyLinkedException;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountLinkRequiredException;
import pl.sgorski.nethelt.webapi.exception.oauth2.IncompleteOAuth2DataException;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth.factory.OAuth2UserInfoFactory;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2AccountLinkService;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2CommonLoginService;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2GithubEmailService;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2ContextPayload;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2Mode;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2PayloadResolver;

@Slf4j
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
      oauthUser = overrideWithGithubUser(userRequest, oauthUser);
    }

    var payload = oAuth2PayloadResolver.consume().orElse(null);
    var context =
        new OAuth2LoginContext(
            oauthUser,
            provider,
            OAuth2UserInfoFactory.create(provider, oauthUser.getAttributes()),
            isLinkMode(payload),
            getUserId(payload).orElse(null));

    log.debug(
        "Processing user {} from OAuth2 provider: {}", context.userInfo().getEmail(), providerStr);
    try {
      return handleRequest(context);
    } catch (AccountLinkRequiredException ex) {
      var error = new OAuth2Error("account-link-required", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    } catch (AccountAlreadyLinkedException ex) {
      var error = new OAuth2Error("account-already-linked", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    } catch (IncompleteOAuth2DataException ex) {
      var error = new OAuth2Error("oauth2-incomplete-data", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    }
  }

  private OAuth2User overrideWithGithubUser(OAuth2UserRequest userRequest, OAuth2User oauthUser) {
    var accessToken = userRequest.getAccessToken().getTokenValue();
    var githubEmail = oAuth2GithubEmailService.getGithubAccountEmail(accessToken);
    var modifiedAttributes = new HashMap<>(oauthUser.getAttributes());
    modifiedAttributes.put("email", githubEmail);
    oauthUser = new DefaultOAuth2User(oauthUser.getAuthorities(), modifiedAttributes, "id");
    return oauthUser;
  }

  private Optional<Long> getUserId(@Nullable OAuth2ContextPayload payload) {
    return Optional.ofNullable(payload).map(OAuth2ContextPayload::userId);
  }

  private boolean isLinkMode(@Nullable OAuth2ContextPayload payload) {
    return payload != null && payload.mode() == OAuth2Mode.LINK;
  }

  private OAuth2User handleRequest(OAuth2LoginContext context) {
    if (context.linkMode()) {
      return oAuth2AccountLinkService.handle(context);
    } else {
      return oAuth2CommonLoginService.handle(context);
    }
  }
}
