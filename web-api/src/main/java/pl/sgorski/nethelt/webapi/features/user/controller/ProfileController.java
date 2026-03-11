package pl.sgorski.nethelt.webapi.features.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.service.AuthService;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordChangeRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.PasswordSetRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@RestController
@RequestMapping(path = "/profile", version = "1")
@RequiredArgsConstructor
public final class ProfileController {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper userMapper;

    //TODO: replace UserResponse with ProfileResponse (user + identities)
    @GetMapping
    public ResponseEntity<UserResponse> showProfile(Authentication authentication) {
        var user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> setLocalPassword(
            @RequestBody @Valid PasswordSetRequest request,
            Authentication authentication
    ) {
        var user = getAuthenticatedUser(authentication);
        authService.setLocalPassword(user, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid PasswordChangeRequest request,
            Authentication authentication
    ) {
        var user = getAuthenticatedUser(authentication);
        authService.changePassword(user, request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        var email = authentication.getName();
        return userService.getUser(email);
    }
}
