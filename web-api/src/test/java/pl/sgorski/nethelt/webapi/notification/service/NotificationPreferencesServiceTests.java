package pl.sgorski.nethelt.webapi.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationPreferencesNotFoundException;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationPreferencesRepository;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class NotificationPreferencesServiceTests {

  @Mock private NotificationPreferencesRepository repository;
  @InjectMocks private NotificationPreferencesService service;

  @Test
  void getPreferences_shouldReturnPreferences_whenExists() {
    var user = TestUserFactory.createLocalUser();
    when(repository.findByUserId(1L)).thenReturn(Optional.of(user.getNotificationPreferences()));

    var preferences = service.getPreferences(1L);

    assertSame(user.getNotificationPreferences(), preferences);
  }

  @Test
  void getPreferences_shouldThrow_whenNotFound() {
    when(repository.findByUserId(1L)).thenReturn(Optional.empty());

    assertThrows(NotificationPreferencesNotFoundException.class, () -> service.getPreferences(1L));
  }

  @Test
  void enableChannel_shouldEnableChannel() {
    var user = TestUserFactory.createLocalUser();
    var preferences = user.getNotificationPreferences();
    preferences.getEnabledChannels().remove(NotificationChannel.EMAIL);
    when(repository.findByUserId(1L)).thenReturn(Optional.of(preferences));

    var updatedPreferences = service.enableChannel(1L, NotificationChannel.EMAIL);

    assertTrue(preferences.getEnabledChannels().contains(NotificationChannel.EMAIL));
    assertSame(preferences, updatedPreferences);
  }

  @Test
  void disableChannel_shouldDisableChannel() {
    var user = TestUserFactory.createLocalUser();
    var preferences = user.getNotificationPreferences();
    preferences.getEnabledChannels().add(NotificationChannel.EMAIL);
    when(repository.findByUserId(1L)).thenReturn(Optional.of(preferences));

    var updatedPreferences = service.disableChannel(1L, NotificationChannel.EMAIL);

    assertFalse(preferences.getEnabledChannels().contains(NotificationChannel.EMAIL));
    assertSame(preferences, updatedPreferences);
  }
}
