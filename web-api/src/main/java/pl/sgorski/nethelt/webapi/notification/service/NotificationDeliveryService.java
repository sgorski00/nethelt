package pl.sgorski.nethelt.webapi.notification.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;

@Service
@RequiredArgsConstructor
public final class NotificationDeliveryService {

  private final List<NotificationSender> senders;

  public void send(Notification notification, Set<NotificationChannel> channels) {
    // todo: consider adding notification delivery [NET-37]
    channels.forEach(
        channel ->
            senders.stream()
                .filter(sender -> sender.supports(channel))
                .forEach(sender -> sender.send(notification)));
  }
}
