package pl.sgorski.nethelt.webapi.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.jwt.JwtService;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public final class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${nh.frontend.oauth-success-url}")
    private String frontendOauth2SuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        var principal = (OAuth2User) Objects.requireNonNull(authentication.getPrincipal(), "Authentication failed");
        var email = (String) Objects.requireNonNull(principal.getAttribute("email"), "OAuth email is missing");
        var user = userService.getUser(email);
        if(user.getAuthProvider() == AuthProvider.LOCAL) {
            throw new AccessDeniedException("Local users are not allowed to login with OAuth");
        }
        var token = jwtService.generateAccessToken(user);

        //TODO: add refresh token here as a cookie
        var redirectUrl = String.format("%s?token=%s", frontendOauth2SuccessUrl, token);
        response.sendRedirect(redirectUrl);
    }
}
