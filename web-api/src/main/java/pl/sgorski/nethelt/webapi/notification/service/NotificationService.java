package pl.sgorski.nethelt.webapi.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserService userService;

  public Page<Notification> getNotifications(Long userId, boolean showRead, Pageable pageable) {
    return notificationRepository.findByUserId(userId, showRead, pageable);
  }

  @Transactional
  public Notification create(NotificationCommand command) {
    log.info(
        "Creating notification for user with ID {}: {} - {}",
        command.userId(),
        command.title(),
        command.content());
    var user = userService.getUser(command.userId());
    var notification = new Notification(user, command.title(), command.content());
    log.info(
        "Notification created with ID {} for user with ID {}", notification.getId(), user.getId());
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

  public Long getUnreadNotificationsCount(Long userId) {
    return notificationRepository.countByUserIdAndReadAtIsNull(userId);
  }
}
