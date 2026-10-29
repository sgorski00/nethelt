package pl.sgorski.nethelt.webapi.features.discovery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanTimeoutServiceTests {

  private static final Duration TIMEOUT = Duration.ofMinutes(10);

  @Mock private DiscoveryScanRepository discoveryScanRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  private DiscoveryScanTimeoutService discoveryScanTimeoutService;

  @BeforeEach
  void setUp() {
    discoveryScanTimeoutService =
        new DiscoveryScanTimeoutService(
            discoveryScanRepository, new DiscoveryScanProperties(TIMEOUT), eventPublisher);
  }

  @Test
  void findExpiredScanIds_shouldReturnScansInProgressRequestedBeforeTimeout() {
    when(discoveryScanRepository.findIdsByStatusInAndRequestedAtBefore(
            eq(List.of(DiscoveryScanStatus.PENDING, DiscoveryScanStatus.RUNNING)),
            argThat(deadline -> deadline.isBefore(Instant.now().minus(TIMEOUT).plusSeconds(5)))))
        .thenReturn(List.of(1L, 2L));

    assertEquals(List.of(1L, 2L), discoveryScanTimeoutService.findExpiredScanIds());
  }

  @Test
  void timeOut_shouldTimeOutScanAndPublishEvent() {
    var scan = scan(Instant.now().minus(TIMEOUT).minusSeconds(1));
    when(discoveryScanRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(scan));

    discoveryScanTimeoutService.timeOut(1L);

    assertEquals(DiscoveryScanStatus.TIMED_OUT, scan.getStatus());
    verify(eventPublisher)
        .publishEvent(
            (Object)
                argThat(
                    event ->
                        event instanceof DiscoveryScanStatusChangedEvent e
                            && e.status() == DiscoveryScanStatus.TIMED_OUT));
  }

  @Test
  void timeOut_shouldDoNothing_whenScanIsNoLongerExpired() {
    var scan = scan(Instant.now());
    when(discoveryScanRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(scan));

    discoveryScanTimeoutService.timeOut(1L);

    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void timeOut_shouldDoNothing_whenScanNotFound() {
    when(discoveryScanRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

    discoveryScanTimeoutService.timeOut(1L);

    verifyNoInteractions(eventPublisher);
  }

  private static DiscoveryScan scan(Instant requestedAt) {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(1L), null);
    ReflectionTestUtils.setField(scan, "requestedAt", requestedAt);
    return scan;
  }
}
