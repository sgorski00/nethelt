package pl.sgorski.nethelt.webapi.notification.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;

@ExtendWith(MockitoExtension.class)
public class NotificationDeliveryCleanupJobTests {

  @Mock private NotificationDeliveryService notificationDeliveryService;
  @InjectMocks private NotificationDeliveryCleanupJob notificationDeliveryCleanupJob;

  @Test
  void task_shouldRunWithoutErrors() {
    assertDoesNotThrow(() -> notificationDeliveryCleanupJob.cleanUpFinishedDeliveries());
    verify(notificationDeliveryService).deleteFinishedDeliveries();
  }

  @Test
  void task_shouldNotThrow_whenServiceThrows() {
    when(notificationDeliveryService.deleteFinishedDeliveries())
        .thenThrow(new RuntimeException("DB down"));

    assertDoesNotThrow(() -> notificationDeliveryCleanupJob.cleanUpFinishedDeliveries());
  }
}
