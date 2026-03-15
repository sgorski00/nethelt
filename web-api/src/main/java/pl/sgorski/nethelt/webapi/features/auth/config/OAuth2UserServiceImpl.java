package pl.sgorski.nethelt.webapi.features.auth.config;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.service.*;
import pl.sgorski.nethelt.webapi.security.session.OAuth2SessionService;

import java.util.HashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public final class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final OAuth2CommonLoginService oAuth2CommonLoginService;
    private final OAuth2AccountLinkService oAuth2AccountLinkService;
    private final OAuth2SessionService oAuth2SessionService;
    private final OAuth2GithubEmailService oAuth2GithubEmailService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Loading user from OAuth2 provider");
        var oauthUser = super.loadUser(userRequest);
        var providerStr = userRequest.getClientRegistration().getRegistrationId();
        var provider = AuthProvider.fromString(providerStr);
        if(provider.equals(AuthProvider.GITHUB)) {
            var accessToken = userRequest.getAccessToken().getTokenValue();
            var githubEmail = oAuth2GithubEmailService.getGithubAccountEmail(accessToken);
            var modifiedAttributes = new HashMap<>(oauthUser.getAttributes());
            modifiedAttributes.put("email", githubEmail);
            oauthUser = new DefaultOAuth2User(oauthUser.getAuthorities(), modifiedAttributes, "id");
        }

        var session = getSession();
        var context = new OAuth2LoginContext(
                oauthUser,
                provider,
                OAuth2UserInfoFactory.create(provider, oauthUser.getAttributes()),
                oAuth2SessionService.isLinkMode(session),
                oAuth2SessionService.getOAuthLinkUserId(session)
        );
        oAuth2SessionService.clearOAuthAttributes(session);

        log.debug("Processing user {} from OAuth2 provider: {}", context.userInfo().getEmail(), providerStr);
        if (context.linkMode()) {
            return oAuth2AccountLinkService.handle(context);
        }
        return oAuth2CommonLoginService.handle(context);
    }

    private HttpSession getSession() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest()
                .getSession(true);
    }
}
