package pl.sgorski.nethelt.webapi.exception.notification;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class NotificationNotFoundException extends NotFoundException {
  public NotificationNotFoundException() {
    super("Notification not found");
  }
}
