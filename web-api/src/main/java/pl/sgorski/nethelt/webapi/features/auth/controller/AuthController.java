package pl.sgorski.nethelt.webapi.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.auth.service.AuthService;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;

@RestController
@RequestMapping(value = "/auth", version = "1")
@RequiredArgsConstructor
public final class AuthController {

    private final AuthService authService;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid RegisterUserRequest request
    ) {
        var command = authMapper.toCommand(request);
        var user = authService.registerUser(command);
        return ResponseEntity.status(201).body(userMapper.toResponse(user));
    }

}
