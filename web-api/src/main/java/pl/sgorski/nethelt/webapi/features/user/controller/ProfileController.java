package pl.sgorski.nethelt.webapi.features.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.service.LocalAuthService;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordChangeRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordSetRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

import java.util.Locale;

import static pl.sgorski.nethelt.webapi.security.session.OAuthSessionAttributes.*;

@Log4j2
@RestController
@RequestMapping(path = "/profile", version = "1")
@RequiredArgsConstructor
public final class ProfileController {

    private final LocalAuthService localAuthService;
    private final UserMapper userMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    //TODO: replace UserResponse with ProfileResponse (user + identities)
    @GetMapping
    public ResponseEntity<UserResponse> showProfile(Authentication authentication) {
        var user = authenticatedUserResolver.requireUser(authentication);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> setLocalPassword(
            @RequestBody @Valid PasswordSetRequest request,
            Authentication authentication
    ) {
        var user = authenticatedUserResolver.requireUser(authentication);
        localAuthService.setLocalPassword(user, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid PasswordChangeRequest request,
            Authentication authentication
    ) {
        var user = authenticatedUserResolver.requireUser(authentication);
        localAuthService.changePassword(user, request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/link/{provider}")
    public ResponseEntity<Void> linkAccount(
            @SuppressWarnings("SpringMvcPathVariableDeclarationInspection") @PathVariable("provider") AuthProvider provider,
            HttpServletRequest request,
            Authentication authentication
    ) {
        var userId = authenticatedUserResolver.requireUser(authentication).getId();
        addOAuth2SessionAttributes(request, userId);

        var redirectPath = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/oauth2/authorization/")
                .path(provider.name().toLowerCase(Locale.ROOT))
                .build().toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectPath).build();
    }

    private void addOAuth2SessionAttributes(HttpServletRequest request, Long userId) {
        var session = request.getSession(true);
        session.setAttribute(OAUTH_MODE.getAttributeName(), "link");
        session.setAttribute(OAUTH_LINK_USER_ID.getAttributeName(), userId);
    }
}
