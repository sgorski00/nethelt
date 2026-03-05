package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.UserAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.Role;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.dto.command.RegisterUserCommand;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public final class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User registerUser(RegisterUserCommand command) {
        throwIfUserIsPresent(command.email(), command.username());

        var user = userMapper.toEntity(command);

        var hashPassword = Objects.requireNonNull(
                passwordEncoder.encode(command.password()),
                "Password encoding failed"
        );

        user.setRole(Role.USER);
        user.setPasswordHash(hashPassword);
        return userRepository.save(user);
    }


    public User getUser(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private void throwIfUserIsPresent(String email, String username) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)
                || userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw new UserAlreadyExistsException();
        }
    }
}
