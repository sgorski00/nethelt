package pl.sgorski.nethelt.webapi.exception.notification;

import pl.sgorski.nethelt.webapi.exception.application.NotAllowedException;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;

public final class NotificationChannelNotConfigurableException extends NotAllowedException {
  public NotificationChannelNotConfigurableException(NotificationChannel channel) {
    super("Notification channel " + channel + " cannot be disabled");
  }
}
