package pl.sgorski.nethelt.webapi.utils;

import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;

public final class TestNotificationFactory {

  public static Notification createNotification() {
    return createNotification("Test notification", "Test content");
  }

  public static Notification createNotification(String title, String content) {
    var user = TestUserFactory.createLocalUser();
    return createNotification(user, title, content);
  }

  public static Notification createNotification(User user, String title, String content) {
    return new Notification(user, title, content);
  }
}
