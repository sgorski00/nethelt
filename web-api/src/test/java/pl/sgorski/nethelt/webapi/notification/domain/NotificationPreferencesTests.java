package pl.sgorski.nethelt.webapi.notification.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

public class NotificationPreferencesTests {

  @Test
  void constructor_shouldEnableAllChannelsByDefault() {
    var user = TestUserFactory.createLocalUser();
    var preferences = new NotificationPreferences(user);

    for (var channel : NotificationChannel.values()) {
      assertTrue(preferences.isChannelEnabled(channel));
    }
  }

  @Test
  void constructor_shouldAssignCorrectUser() {
    var user = TestUserFactory.createLocalUser();
    var preferences = new NotificationPreferences(user);

    assertSame(user, preferences.getUser());
  }

  @Test
  void isChannelEnabled_shouldReturnTrue_whenChannelIsEnabled() {
    var user = TestUserFactory.createLocalUser();
    var preferences = new NotificationPreferences(user);

    var result = preferences.isChannelEnabled(NotificationChannel.EMAIL);

    assertTrue(result);
  }

  @Test
  void isChannelEnabled_shouldReturnFalse_whenChannelIsDisabled() {
    var user = TestUserFactory.createLocalUser();
    var preferences = new NotificationPreferences(user);
    preferences.disableChannel(NotificationChannel.EMAIL);

    var result = preferences.isChannelEnabled(NotificationChannel.EMAIL);

    assertFalse(result);
  }

  @Test
  void enableChannel_shouldEnableChannel() {
    var user = TestUserFactory.createLocalUser();
    var preferences = new NotificationPreferences(user);
    preferences.disableChannel(NotificationChannel.EMAIL);

    preferences.enableChannel(NotificationChannel.EMAIL);

    assertTrue(preferences.isChannelEnabled(NotificationChannel.EMAIL));
  }

  @Test
  void disableChannel_shouldDisableChannel() {
    var user = TestUserFactory.createLocalUser();
    var preferences = new NotificationPreferences(user);

    preferences.disableChannel(NotificationChannel.EMAIL);

    assertFalse(preferences.isChannelEnabled(NotificationChannel.EMAIL));
  }
}
