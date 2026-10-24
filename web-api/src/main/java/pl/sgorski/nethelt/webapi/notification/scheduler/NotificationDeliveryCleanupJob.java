package pl.sgorski.nethelt.webapi.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryCleanupJob {

  private final NotificationDeliveryService notificationDeliveryService;

  @Scheduled(cron = "0 30 3 * * *")
  public void cleanUpFinishedDeliveries() {
    try {
      log.debug("Starting cleanup of finished notification deliveries...");
      var deleted = notificationDeliveryService.deleteFinishedDeliveries();
      log.info("Notification delivery cleanup completed, {} deliveries deleted", deleted);
    } catch (Exception e) {
      log.error("Error during notification delivery cleanup", e);
    }
  }
}
