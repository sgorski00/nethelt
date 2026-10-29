package pl.sgorski.nethelt.webapi.features.discovery.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.discovery.service.DiscoveryScanTimeoutService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveryScanTimeoutJob {

  private final DiscoveryScanTimeoutService discoveryScanTimeoutService;

  @Scheduled(fixedDelayString = "${discovery.scan.timeout-check-interval}")
  public void timeOutExpiredScans() {
    try {
      var scanIds = discoveryScanTimeoutService.findExpiredScanIds();
      log.debug("Timing out {} discovery scans", scanIds.size());
      scanIds.forEach(this::timeOut);
    } catch (Exception e) {
      log.error("Error during discovery scan timeout check", e);
    }
  }

  private void timeOut(Long scanId) {
    try {
      discoveryScanTimeoutService.timeOut(scanId);
    } catch (Exception e) {
      log.error("Failed to time out discovery scan with ID {}", scanId, e);
    }
  }
}
