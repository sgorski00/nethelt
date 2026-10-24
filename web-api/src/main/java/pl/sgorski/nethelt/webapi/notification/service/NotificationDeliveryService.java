package pl.sgorski.nethelt.webapi.notification.service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.notification.config.NotificationDeliveryProperties;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationDelivery;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationDeliveryStatus;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationDeliveryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

  private final List<NotificationSender> senders;
  private final NotificationDeliveryRepository notificationDeliveryRepository;
  private final NotificationDeliveryProperties properties;

  public void send(Notification notification, Set<NotificationChannel> channels) {
    channels.forEach(
        channel -> {
          if (findSender(channel).isEmpty()) {
            log.warn(
                "No sender supports channel {}, skipping notification with ID {}",
                channel,
                notification.getId());
            return;
          }
          deliver(
              notificationDeliveryRepository.save(new NotificationDelivery(notification, channel)));
        });
  }

  public void retryFailedDeliveries() {
    var deliveries =
        notificationDeliveryRepository.findDue(
            NotificationDeliveryStatus.FAILED,
            Instant.now(),
            PageRequest.ofSize(properties.retryBatchSize()));
    log.debug("Retrying {} failed notification deliveries", deliveries.size());
    deliveries.forEach(this::deliver);
  }

  @Transactional
  public int deleteFinishedDeliveries() {
    return notificationDeliveryRepository.deleteByStatusInAndUpdatedAtBefore(
        EnumSet.of(NotificationDeliveryStatus.SENT, NotificationDeliveryStatus.EXHAUSTED),
        Instant.now().minus(properties.retention()));
  }

  private void deliver(NotificationDelivery delivery) {
    try {
      var sender =
          findSender(delivery.getChannel())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "No sender supports channel " + delivery.getChannel()));
      sender.send(delivery.getNotification());
      delivery.markSent();
    } catch (Exception e) {
      log.warn(
          "Failed to deliver notification delivery with ID {} via {}: {}",
          delivery.getId(),
          delivery.getChannel(),
          e.getMessage(),
          e);
      delivery.markFailed(errorMessage(e), properties.maxAttempts(), properties.retryBackoff());
    }
    notificationDeliveryRepository.save(delivery);
  }

  private Optional<NotificationSender> findSender(NotificationChannel channel) {
    return senders.stream().filter(sender -> sender.supports(channel)).findFirst();
  }

  private String errorMessage(Exception e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }
}
