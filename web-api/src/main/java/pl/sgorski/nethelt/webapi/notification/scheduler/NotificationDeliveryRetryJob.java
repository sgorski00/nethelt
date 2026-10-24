package pl.sgorski.nethelt.webapi.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryRetryJob {

  private final NotificationDeliveryService notificationDeliveryService;

  @Scheduled(fixedDelayString = "${notification.delivery.retry-interval}")
  public void retryFailedDeliveries() {
    try {
      notificationDeliveryService.retryFailedDeliveries();
    } catch (Exception e) {
      log.error("Error during notification delivery retry", e);
    }
  }
}
