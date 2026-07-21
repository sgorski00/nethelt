package pl.sgorski.nethelt.webapi.notification.service;

import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;

public interface NotificationSender {

  boolean supports(NotificationChannel channel);

  void send(Notification notification);
}
