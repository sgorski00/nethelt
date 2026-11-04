package pl.sgorski.nethelt.agent.scheduler;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.service.MonitoringResultSender;

@Slf4j
@Component
@RequiredArgsConstructor
public final class ResultBufferFlushScheduler {

  private final MonitoringResultSender monitoringResultSender;

  @Scheduled(
      fixedDelayString = "${buffer.flush-interval-seconds}",
      initialDelayString = "${buffer.flush-interval-seconds}",
      timeUnit = TimeUnit.SECONDS)
  void flush() {
    try {
      monitoringResultSender.flushBuffer();
    } catch (Exception e) {
      log.error("Error while flushing result buffer: {}", e.getMessage(), e);
    }
  }
}
