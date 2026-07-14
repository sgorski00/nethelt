package pl.sgorski.nethelt.webapi.security.oauth2;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import pl.sgorski.nethelt.webapi.exception.oauth2.InvalidOAuth2UserInfoException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.addon.OAuth2GithubEmailService;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.connect.OAuth2ConnectService;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2ContextPayload;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2PayloadResolver;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.factory.OAuth2UserInfoFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public final class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

  private final List<OAuth2ConnectService> oAuth2ConnectServices;
  private final OAuth2PayloadResolver oAuth2PayloadResolver;
  private final OAuth2GithubEmailService oAuth2GithubEmailService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    try {
      log.debug("Loading user from OAuth2 provider");
      var oauthUser = loadOAuth2User(userRequest);
      var payload = oAuth2PayloadResolver.consume().orElse(OAuth2ContextPayload.withDefaults());
      var provider = getProvider(userRequest);
      if (provider.equals(AuthProvider.GITHUB)) {
        oauthUser = overrideWithGithubUser(userRequest, oauthUser);
      }
      var context = createContext(oauthUser, provider, payload);
      return handleRequest(payload.mode(), context);
    } catch (AccountLinkRequiredException ex) {
      var error = new OAuth2Error("account-link-required", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    } catch (AccountAlreadyLinkedException ex) {
      var error = new OAuth2Error("account-already-linked", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    } catch (InvalidOAuth2UserInfoException ex) {
      var error = new OAuth2Error("invalid-oauth2-user-info", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    } catch (IncompleteOAuth2DataException | IllegalStateException ex) {
      var error = new OAuth2Error("oauth2-incomplete-data", ex.getMessage(), null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    }
  }

  OAuth2User loadOAuth2User(OAuth2UserRequest userRequest) {
    return super.loadUser(userRequest);
  }

  private AuthProvider getProvider(OAuth2UserRequest userRequest) {
    var providerStr = userRequest.getClientRegistration().getRegistrationId();
    return AuthProvider.fromString(providerStr);
  }

  private OAuth2User overrideWithGithubUser(OAuth2UserRequest userRequest, OAuth2User oauthUser) {
    var accessToken = userRequest.getAccessToken().getTokenValue();
    var githubEmail = oAuth2GithubEmailService.getGithubAccountEmail(accessToken);
    var modifiedAttributes = new HashMap<>(oauthUser.getAttributes());
    modifiedAttributes.put("email", githubEmail);
    oauthUser = new DefaultOAuth2User(oauthUser.getAuthorities(), modifiedAttributes, "id");
    return oauthUser;
  }

  private OAuth2LoginContext createContext(
      OAuth2User oauthUser, AuthProvider provider, OAuth2ContextPayload payload) {
    return new OAuth2LoginContext(
        oauthUser,
        provider,
        OAuth2UserInfoFactory.create(provider, oauthUser.getAttributes()),
        payload.userId());
  }

  private OAuth2User handleRequest(OAuth2Mode mode, OAuth2LoginContext context) {
    for (var service : oAuth2ConnectServices) {
      if (service.supports(mode)) {
        return service.handle(context);
      }
    }
    throw new IllegalStateException("No suitable OAuth2ConnectService found for the given context");
  }
}
