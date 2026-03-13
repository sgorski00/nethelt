package pl.sgorski.nethelt.webapi.features.auth.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.sgorski.nethelt.webapi.exception.AccountLinkRequiredException;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.domain.OAuthUserInfo;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuthUserInfoFactory;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

import static pl.sgorski.nethelt.webapi.security.session.OAuthSessionAttributes.*;

@Log4j2
@Service
@RequiredArgsConstructor
public final class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final AuthMapper authMapper;
    private final UserService userService;
    private final UserIdentityService userIdentityService;

    @Override
    public @Nullable OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Loading user from OAuth2 provider");
        var oauthUser = super.loadUser(userRequest);
        var providerStr = userRequest.getClientRegistration().getRegistrationId();
        var provider = AuthProvider.fromString(providerStr);
        var userInfo = OAuthUserInfoFactory.create(provider, oauthUser.getAttributes());
        var session = getSession();
        var isLinkMode = isLinkMode(session);
        var userId = getOAuthLinkUserId(session);
        clearSession(session);

        log.debug("Processing user {} from OAuth2 provider: {}", userInfo.getEmail(), providerStr);
        if(isLinkMode) {
            return handleLinkMode(userInfo, provider, userId);
        } else {
            return handleCasualOAuth(userInfo, oauthUser, provider);
        }
    }

    private OAuth2User handleCasualOAuth(OAuthUserInfo userInfo, OAuth2User oauthUser, AuthProvider provider) {
        log.debug("Entering OAuth2 login/register mode");
        if(userIdentityService.isUserIdentityPresent(userInfo.getProviderId(), userInfo.getProvider())) {
            log.debug("Existing oauth user identity detected: {}, {}. Logging in...", userInfo.getEmail(), userInfo.getProvider().name());
            return oauthUser;
        }

        if(userService.isUserPresent(userInfo.getEmail())) {
            log.warn("Local user with email {} already exists. OAuthLogin blocked", userInfo.getEmail());
            throw new AccountLinkRequiredException();
        }

        var user = new User();
        user.setEmail(userInfo.getEmail());
        log.debug("New user {} created. Linking identity {}...", user.getEmail(), provider.name());
        var identity = authMapper.toIdentity(userInfo);
        user.addIdentity(identity);
        userService.save(user);
        return oauthUser;
    }

    private @Nullable OAuth2User handleLinkMode(OAuthUserInfo userInfo, AuthProvider provider, @Nullable Long userId) {
        log.debug("Entering OAuth2 account link mode");
        if(userIdentityService.isUserIdentityPresent(userInfo.getProviderId(), userInfo.getProvider())) {
            log.debug("Someone is using account: {} [{}] already.", userInfo.getEmail(), provider.name());
            throw new IllegalStateException("Account is already linked to another user");
        }
        if(userId == null) {
            log.error("There is no user id in the session! Cannot link an oauth2 account");
            throw new IllegalStateException("User id is required to link an account");
        }
        var user = userService.getUserWithIdentities(userId);
        log.debug("Linking new identity {} to existing user {}", provider.name(), user.getEmail());
        var identity = authMapper.toIdentity(userInfo);
        user.addIdentity(identity);
        userService.save(user);
        return null;
    }

    private @Nullable Long getOAuthLinkUserId(HttpSession session) {
        return (Long) session.getAttribute(OAUTH_LINK_USER_ID.getAttributeName());
    }

    private boolean isLinkMode(HttpSession session) {
        return  "link".equals(session.getAttribute(OAUTH_MODE.getAttributeName()));
    }

    private HttpSession getSession() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest()
                .getSession(true);
    }

    private void clearSession(HttpSession session) {
        session.removeAttribute(OAUTH_MODE.getAttributeName());
        session.removeAttribute(OAUTH_LINK_USER_ID.getAttributeName());
    }
}
