package pl.sgorski.nethelt.webapi.features.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@RestController
@RequestMapping(path = "/profile", version = "1")
@RequiredArgsConstructor
public final class ProfileController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<UserResponse> showProfile(Authentication authentication) {
        var username = authentication.getName();
        var user = userService.getUser(username);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
