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
public class NotificationDeliveryRetryJobTests {

  @Mock private NotificationDeliveryService notificationDeliveryService;
  @InjectMocks private NotificationDeliveryRetryJob notificationDeliveryRetryJob;

  @Test
  void task_shouldRunWithoutErrors() {
    assertDoesNotThrow(() -> notificationDeliveryRetryJob.retryFailedDeliveries());
    verify(notificationDeliveryService).retryFailedDeliveries();
  }

  @Test
  void task_shouldNotThrow_whenServiceThrows() {
    doThrow(new RuntimeException("DB down"))
        .when(notificationDeliveryService)
        .retryFailedDeliveries();

    assertDoesNotThrow(() -> notificationDeliveryRetryJob.retryFailedDeliveries());
  }
}
