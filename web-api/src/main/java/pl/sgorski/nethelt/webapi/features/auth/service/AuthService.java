package pl.sgorski.nethelt.webapi.features.auth.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.UserAlreadyExistsException;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.LoginUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.auth.dto.request.LoginRequest;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.user.domain.Role;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public final class AuthService {

    private final AuthMapper authMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User registerUser(RegisterUserCommand command) {
        if(userService.isUserPresent(command.email(), command.username())) {
            throw new UserAlreadyExistsException();
        }

        var user = authMapper.toEntity(command);

        var hashPassword = Objects.requireNonNull(
                passwordEncoder.encode(command.password()),
                "Password encoding failed"
        );

        user.setRole(Role.USER);
        user.setPasswordHash(hashPassword);
        return userService.save(user);
    }

    /**
     * Validate user's login request and returns JWT token if credentials are correct
     */
    public String login(LoginUserCommand command) {
        var user = userService.getUser(command.username());
        if(!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password"); //TODO: replace this with domain exception
        }

        return jwtService.generateAccessToken(user);
    }
}
