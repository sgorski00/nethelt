package pl.sgorski.nethelt.webapi.notification.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final List<NotificationSender> senders;
  private final NotificationRepository notificationRepository;
  private final UserService userService;

  public void send(Notification notification, Set<NotificationChannel> channels) {
    channels.forEach(
        channel ->
            senders.stream()
                .filter(sender -> sender.supports(channel))
                .forEach(sender -> sender.send(notification)));
  }

  @Transactional
  public Notification create(NotificationCommand command) {
    var user = userService.getUser(command.userId());
    var notification = new Notification(user, command.title(), command.content());
    return notificationRepository.save(notification);
  }

  @Transactional
  public void read(Long notificationId) {
    var notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(NotificationNotFoundException::new);
    notification.markAsRead();
  }
}
