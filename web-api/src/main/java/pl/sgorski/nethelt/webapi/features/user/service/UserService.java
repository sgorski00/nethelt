package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public final class UserService {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public User getUser(String identifier) {
        return userRepository.findByUsernameAndDeletedAtIsNull(identifier)
                .or(() -> userRepository.findByEmailAndDeletedAtIsNull(identifier))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + identifier));
    }

    public boolean isUserPresent(String email, String username) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email)
                || userRepository.existsByUsernameAndDeletedAtIsNull(username);
    }
}
