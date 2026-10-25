package pl.sgorski.nethelt.webapi.notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationDeliverySkippedException;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.notification.service.NotificationSender;
import pl.sgorski.nethelt.webapi.web.websocket.UserMessageSender;

@Component
@RequiredArgsConstructor
public final class WebSocketNotificationSender implements NotificationSender {

  public static final String DESTINATION = "/queue/notifications";

  private final UserMessageSender userMessageSender;
  private final NotificationMapper notificationMapper;

  @Override
  public boolean supports(NotificationChannel channel) {
    return channel == NotificationChannel.WEBSOCKET;
  }

  @Override
  public void send(Notification notification) {
    var sent =
        userMessageSender.send(
            notification.getUser().getId(),
            DESTINATION,
            notificationMapper.toResponse(notification));
    if (!sent) {
      throw new NotificationDeliverySkippedException("User is not connected");
    }
  }
}
