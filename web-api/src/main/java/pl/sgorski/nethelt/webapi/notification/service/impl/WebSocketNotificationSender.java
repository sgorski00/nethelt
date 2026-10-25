package pl.sgorski.nethelt.webapi.notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationDeliverySkippedException;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.notification.service.NotificationSender;

@Component
@RequiredArgsConstructor
public final class WebSocketNotificationSender implements NotificationSender {

  public static final String DESTINATION = "/queue/notifications";

  private final SimpMessagingTemplate messagingTemplate;
  private final SimpUserRegistry userRegistry;
  private final NotificationMapper notificationMapper;

  @Override
  public boolean supports(NotificationChannel channel) {
    return channel == NotificationChannel.WEBSOCKET;
  }

  @Override
  public void send(Notification notification) {
    var username = notification.getUser().getUsername();
    if (userRegistry.getUser(username) == null) {
      throw new NotificationDeliverySkippedException("User is not connected");
    }
    messagingTemplate.convertAndSendToUser(
        username, DESTINATION, notificationMapper.toResponse(notification));
  }
}
