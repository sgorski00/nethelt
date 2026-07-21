package pl.sgorski.nethelt.webapi.notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.infrastructure.mail.EmailSender;
import pl.sgorski.nethelt.webapi.notification.service.NotificationSender;

@Component
@RequiredArgsConstructor
public final class EmailNotificationSender implements NotificationSender {

  private final EmailSender emailSender;

  @Override
  public boolean supports(NotificationChannel channel) {
    return channel == NotificationChannel.EMAIL;
  }

  @Override
  public void send(Notification notification) {
    emailSender.send(
        notification.getUser().getEmail(), notification.getTitle(), notification.getContent());
  }
}
