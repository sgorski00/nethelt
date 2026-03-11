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
        if(user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return userRepository.save(user);
    }

    public User getUser(String identifier) {
        return userRepository.findByUsernameAndDeletedAtIsNull(identifier)
                .or(() -> userRepository.findByEmailAndDeletedAtIsNull(identifier))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + identifier));
    }

    public boolean isUserPresent(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    public boolean isUserPresent(String email, String username) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email)
                || userRepository.existsByUsernameAndDeletedAtIsNull(username);
    }
}
