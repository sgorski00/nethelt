package pl.sgorski.nethelt.webapi.features.discovery.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryScanTimeoutService {

  private final DiscoveryScanRepository discoveryScanRepository;
  private final DiscoveryScanProperties discoveryScanProperties;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public List<Long> findExpiredScanIds() {
    var deadline = Instant.now().minus(discoveryScanProperties.timeout());
    return discoveryScanRepository.findIdsByStatusInAndRequestedAtBefore(
        DiscoveryScanStatus.IN_PROGRESS, deadline);
  }

  @Transactional
  public void timeOut(Long scanId) {
    discoveryScanRepository
        .findByIdForUpdate(scanId)
        .filter(scan -> scan.timeOutIfExpired(Instant.now(), discoveryScanProperties.timeout()))
        .ifPresent(
            scan -> {
              log.info("Discovery scan with ID {} has timed out", scanId);
              eventPublisher.publishEvent(DiscoveryScanStatusChangedEvent.of(scan));
            });
  }
}
