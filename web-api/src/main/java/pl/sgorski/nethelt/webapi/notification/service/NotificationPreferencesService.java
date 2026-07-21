package pl.sgorski.nethelt.webapi.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationPreferencesNotFoundException;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationPreferences;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationPreferencesRepository;

@Service
@RequiredArgsConstructor
public class NotificationPreferencesService {

  private final NotificationPreferencesRepository notificationPreferencesRepository;

  public NotificationPreferences getPreferences(Long userId) {
    return notificationPreferencesRepository
        .findByUserId(userId)
        .orElseThrow(NotificationPreferencesNotFoundException::new);
  }

  @Transactional
  public NotificationPreferences enableChannel(Long userId, NotificationChannel channel) {
    var preferences = getPreferences(userId);
    preferences.enableChannel(channel);
    return preferences;
  }

  @Transactional
  public NotificationPreferences disableChannel(Long userId, NotificationChannel channel) {
    var preferences = getPreferences(userId);
    preferences.disableChannel(channel);
    return preferences;
  }
}
