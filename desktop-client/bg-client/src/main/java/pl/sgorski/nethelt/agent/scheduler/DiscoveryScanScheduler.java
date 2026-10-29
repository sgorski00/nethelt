package pl.sgorski.nethelt.agent.scheduler;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;
import pl.sgorski.nethelt.agent.exception.MissingCredentialsException;
import pl.sgorski.nethelt.agent.service.DiscoveryScanService;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DiscoveryScanScheduler {

  private final DiscoveryScanService discoveryScanService;

  @Scheduled(
      fixedDelayString = "${scheduler.discovery-poll-interval-seconds}",
      timeUnit = TimeUnit.SECONDS)
  void checkPendingScan() {
    try {
      discoveryScanService.startPendingScan();
    } catch (MissingCredentialsException | CredentialsAccessDeniedException e) {
      log.warn("Skipping discovery scan check: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Error while checking for a pending discovery scan: {}", e.getMessage(), e);
    }
  }
}
