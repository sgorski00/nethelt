package pl.sgorski.nethelt.webapi.features.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.RegisterUserRequest;
import pl.sgorski.nethelt.webapi.features.auth.dto.response.JwtResponse;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
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

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        var token = new JwtResponse(authService.login(authMapper.toCommand(request)));
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid RegisterUserRequest request
    ) {
        var command = authMapper.toCommand(request);
        var user = authService.registerUser(command);
        return ResponseEntity.status(201).body(userMapper.toResponse(user));
    }
}
