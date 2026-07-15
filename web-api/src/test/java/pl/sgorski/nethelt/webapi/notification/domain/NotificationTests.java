package pl.sgorski.nethelt.webapi.notification.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

public class NotificationTests {

  @Test
  void constructor_shouldCreateNotificationWithRequiredData() {
    var user = TestUserFactory.createLocalUser();

    var result = createNotification(user);

    assertSame(user, result.getUser());
    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Content", result.getContent());
    assertNull(result.getReadAt());
  }

  @Test
  void markAsRead_shouldSetReadAtToCurrentTime() {
    var user = TestUserFactory.createLocalUser();
    var notification = createNotification(user);

    notification.markAsRead();

    assertNotNull(notification.getReadAt());
  }

  @Test
  void markAsRead_shouldDoNothing_whenAlreadyRead() {
    var user = TestUserFactory.createLocalUser();
    var notification = createNotification(user);
    notification.markAsRead();

    notification.markAsRead();

    assertTrue(notification.isRead());
  }

  @Test
  void isRead_shouldReturnTrue_whenReadAtIsNotNull() {
    var user = TestUserFactory.createLocalUser();
    var notification = createNotification(user);

    notification.markAsRead();

    assertTrue(notification.isRead());
  }

  @Test
  void isRead_shouldReturnFalse_whenReadAtIsNull() {
    var user = TestUserFactory.createLocalUser();

    var notification = createNotification(user);

    assertFalse(notification.isRead());
  }

  private Notification createNotification(User user) {
    return new Notification(user, "Test Title", "Test Content");
  }
}
