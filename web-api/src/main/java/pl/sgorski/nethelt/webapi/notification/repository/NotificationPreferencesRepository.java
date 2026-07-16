package pl.sgorski.nethelt.webapi.notification.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationPreferences;

public interface NotificationPreferencesRepository
    extends JpaRepository<NotificationPreferences, Long> {
  Optional<NotificationPreferences> findByUserId(Long userId);
}
