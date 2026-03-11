package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.UserNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.Role;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    public User getUser(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    public User getUserWithIdentities(String email) {
        return userRepository.findWithIdentitiesByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    public boolean isUserPresent(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Transactional
    public User findOrCreateByEmail(String email) {
        return userRepository.findWithIdentitiesByEmailAndDeletedAtIsNull(email).orElseGet(() -> {
            var user = new User();
            user.setEmail(email);
            user.setRole(Role.USER);
            return userRepository.save(user);
        });
    }
}
