package pl.sgorski.nethelt.agent.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.service.MonitoringResultSender;

@ExtendWith(MockitoExtension.class)
public class ResultBufferFlushSchedulerTests {

  @Mock private MonitoringResultSender monitoringResultSender;
  @InjectMocks private ResultBufferFlushScheduler resultBufferFlushScheduler;

  @Test
  void flush_shouldFlushBuffer() {
    resultBufferFlushScheduler.flush();

    verify(monitoringResultSender).flushBuffer();
  }

  @Test
  void flush_shouldNotThrow_whenFlushFails() {
    doThrow(new IllegalStateException("failed")).when(monitoringResultSender).flushBuffer();

    assertDoesNotThrow(() -> resultBufferFlushScheduler.flush());
  }
}
