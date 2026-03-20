package pl.sgorski.nethelt.webapi.security.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.exception.domain.IdentityNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.oauth.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth.factory.OAuth2UserInfoFactory;
import pl.sgorski.nethelt.webapi.features.auth.service.CookieResponseHelper;
import pl.sgorski.nethelt.webapi.features.auth.service.RefreshTokenService;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.security.jwt.JwtService;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public final class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserIdentityService identityService;
    private final RefreshTokenService refreshTokenService;
    private final CookieResponseHelper cookieResponseHelper;
    private final JwtService jwtService;

    @Value("${nh.frontend.oauth-success-url}")
    private String frontendOauth2SuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        var oAuth2Token = (OAuth2AuthenticationToken) authentication;
        var provider = AuthProvider.fromString(oAuth2Token.getAuthorizedClientRegistrationId());
        var principal = (OAuth2User) Objects.requireNonNull(authentication.getPrincipal(), "Authentication failed");
        var userInfo = OAuth2UserInfoFactory.create(provider, principal.getAttributes());
        try {
            var identity = identityService.findIdentity(userInfo.getProvider(), userInfo.getProviderId());
            var user = identity.getUser();
            var token = jwtService.generateAccessToken(user);
            var refreshToken = refreshTokenService.generateRefreshToken(user);
            var cookie = cookieResponseHelper.createRefreshTokenCookie(
                    refreshToken.getToken(),
                    refreshTokenService.getExpirationSecond());
            var redirectUrl = String.format("%s?token=%s", frontendOauth2SuccessUrl, token);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect(redirectUrl);
        } catch (IdentityNotFoundException ex) {
            throw new AccessDeniedException("Local users are not allowed to login with OAuth");
        }
    }
}
