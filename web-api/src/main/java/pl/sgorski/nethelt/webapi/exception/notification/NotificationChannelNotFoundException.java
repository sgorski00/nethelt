package pl.sgorski.nethelt.webapi.exception.notification;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class NotificationChannelNotFoundException extends NotFoundException {
  public NotificationChannelNotFoundException() {
    super("Notification channel not found");
  }
}
