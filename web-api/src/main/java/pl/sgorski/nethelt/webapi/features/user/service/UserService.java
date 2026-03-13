package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.UserNotFoundException;
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

    public User getUserWithIdentities(Long id) {
        return userRepository.findWithIdentitiesByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public boolean isUserPresent(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }
}
