package pl.sgorski.nethelt.webapi.features.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.auth.service.OAuthUserInfoFactory;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Log4j2
@Service
@RequiredArgsConstructor
public final class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final AuthMapper authMapper;
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Loading user from OAuth2 provider");
        var oauthUser = super.loadUser(userRequest);

        var providerStr = userRequest.getClientRegistration().getRegistrationId();
        var provider = AuthProvider.valueOf(providerStr.toUpperCase());
        var userInfo = OAuthUserInfoFactory.create(provider, oauthUser.getAttributes());
        log.debug("Loaded user {} from OAuth2 provider: {}", userInfo.getEmail(), providerStr);
        if(!userService.isUserPresent(userInfo.getEmail())) {
            log.debug("New oauth user detected: {}. Saving...", userInfo.getEmail());
            var user = authMapper.toEntity(userInfo);
            userService.save(user);
        }
        return oauthUser;
    }
}
