package pl.sgorski.nethelt.webapi.features.discovery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.domain.device.InvalidIpRangeException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanAckCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanCompleteCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanResultCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanDesktopServiceTests {

  private static final Duration TIMEOUT = Duration.ofMinutes(10);

  @Mock private DiscoveryScanRepository discoveryScanRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  private DiscoveryScanDesktopService discoveryScanDesktopService;

  @BeforeEach
  void setUp() {
    discoveryScanDesktopService =
        new DiscoveryScanDesktopService(
            discoveryScanRepository, new DiscoveryScanProperties(TIMEOUT), eventPublisher);
  }

  @Test
  void getPendingScan_shouldReturnScan_whenPending() {
    var scan = pendingScan();
    when(discoveryScanRepository.findByNetworkId(1L)).thenReturn(Optional.of(scan));

    assertEquals(Optional.of(scan), discoveryScanDesktopService.getPendingScan(1L));
  }

  @Test
  void getPendingScan_shouldReturnEmpty_whenNotPending() {
    var scan = pendingScan();
    scan.start(Ipv4Range.parse("192.168.0.0/24"));
    when(discoveryScanRepository.findByNetworkId(1L)).thenReturn(Optional.of(scan));

    assertTrue(discoveryScanDesktopService.getPendingScan(1L).isEmpty());
  }

  @Test
  void getPendingScan_shouldReturnEmpty_whenExpired() {
    var scan = pendingScan(Instant.now().minus(TIMEOUT).minusSeconds(1));
    when(discoveryScanRepository.findByNetworkId(1L)).thenReturn(Optional.of(scan));

    assertTrue(discoveryScanDesktopService.getPendingScan(1L).isEmpty());
  }

  @Test
  void getPendingScan_shouldReturnEmpty_whenNetworkWasNeverScanned() {
    when(discoveryScanRepository.findByNetworkId(1L)).thenReturn(Optional.empty());

    assertTrue(discoveryScanDesktopService.getPendingScan(1L).isEmpty());
  }

  @Test
  void acknowledgeScan_shouldStartScanWithRangeFromAgent() {
    var scan = pendingScan();
    mockFindForUpdate(scan);

    discoveryScanDesktopService.acknowledgeScan(
        new DiscoveryScanAckCommand(1L, "192.168.0.1", "192.168.0.254"));

    assertEquals(DiscoveryScanStatus.RUNNING, scan.getStatus());
    assertEquals(Ipv4Range.parse("192.168.0.1-192.168.0.254"), scan.getRange());
    assertEquals(List.of(DiscoveryScanStatus.RUNNING), publishedStatuses(1));
  }

  @Test
  void acknowledgeScan_shouldThrow_whenRangeIsInvalid() {
    var command = new DiscoveryScanAckCommand(1L, "192.168.0.254", "192.168.0.1");

    assertThrows(
        InvalidIpRangeException.class, () -> discoveryScanDesktopService.acknowledgeScan(command));
    verifyNoInteractions(discoveryScanRepository);
  }

  @Test
  void acknowledgeScan_shouldThrow_whenExpired() {
    var scan = pendingScan(Instant.now().minus(TIMEOUT).minusSeconds(1));
    mockFindForUpdate(scan);
    var command = new DiscoveryScanAckCommand(1L, "192.168.0.1", "192.168.0.254");

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanDesktopService.acknowledgeScan(command));
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void acknowledgeScan_shouldThrow_whenNetworkWasNeverScanned() {
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.empty());
    var command = new DiscoveryScanAckCommand(1L, "192.168.0.1", "192.168.0.254");

    assertThrows(
        DiscoveryScanNotFoundException.class,
        () -> discoveryScanDesktopService.acknowledgeScan(command));
  }

  @Test
  void completeScan_shouldSaveResultsAndComplete() {
    var scan = runningScan();
    mockFindForUpdate(scan);

    discoveryScanDesktopService.completeScan(
        new DiscoveryScanCompleteCommand(
            1L,
            List.of(
                new DiscoveryScanResultCommand("192.168.0.1", "router", 1),
                new DiscoveryScanResultCommand("192.168.0.20", null, 7))));

    assertEquals(DiscoveryScanStatus.COMPLETED, scan.getStatus());
    assertEquals(2, scan.getResults().size());
    assertEquals(List.of(DiscoveryScanStatus.COMPLETED), publishedStatuses(1));
  }

  @Test
  void completeScan_shouldCompleteWithoutResults() {
    var scan = runningScan();
    mockFindForUpdate(scan);

    discoveryScanDesktopService.completeScan(new DiscoveryScanCompleteCommand(1L, List.of()));

    assertEquals(DiscoveryScanStatus.COMPLETED, scan.getStatus());
    assertTrue(scan.getResults().isEmpty());
  }

  @Test
  void completeScan_shouldThrow_whenIpAddressIsInvalid() {
    mockFindForUpdate(runningScan());
    var command =
        new DiscoveryScanCompleteCommand(
            1L, List.of(new DiscoveryScanResultCommand("192.168.0.300", null, 1)));

    assertThrows(
        DiscoveryScanValidationFailedException.class,
        () -> discoveryScanDesktopService.completeScan(command));
  }

  @Test
  void completeScan_shouldThrow_whenExpired() {
    var scan = runningScan();
    ReflectionTestUtils.setField(scan, "requestedAt", Instant.now().minus(TIMEOUT).minusSeconds(1));
    mockFindForUpdate(scan);
    var command = new DiscoveryScanCompleteCommand(1L, List.of());

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanDesktopService.completeScan(command));
    assertEquals(DiscoveryScanStatus.RUNNING, scan.getStatus());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void failScan_shouldMarkScanAsFailed() {
    var scan = runningScan();
    mockFindForUpdate(scan);

    discoveryScanDesktopService.failScan(1L, "No network interface");

    assertEquals(DiscoveryScanStatus.FAILED, scan.getStatus());
    assertEquals("No network interface", scan.getFailureReason());
    assertEquals(List.of(DiscoveryScanStatus.FAILED), publishedStatuses(1));
  }

  @Test
  void failScan_shouldThrow_whenExpired() {
    mockFindForUpdate(pendingScan(Instant.now().minus(TIMEOUT).minusSeconds(1)));

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanDesktopService.failScan(1L, "error"));
  }

  private List<DiscoveryScanStatus> publishedStatuses(int times) {
    var captor = ArgumentCaptor.forClass(DiscoveryScanStatusChangedEvent.class);
    verify(eventPublisher, times(times)).publishEvent(captor.capture());
    return captor.getAllValues().stream().map(DiscoveryScanStatusChangedEvent::status).toList();
  }

  private void mockFindForUpdate(DiscoveryScan scan) {
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.of(scan));
  }

  private static DiscoveryScan pendingScan() {
    return new DiscoveryScan(TestNetworkFactory.createNetwork(1L), null);
  }

  private static DiscoveryScan pendingScan(Instant requestedAt) {
    var scan = pendingScan();
    ReflectionTestUtils.setField(scan, "requestedAt", requestedAt);
    return scan;
  }

  private static DiscoveryScan runningScan() {
    var scan = pendingScan();
    scan.start(Ipv4Range.parse("192.168.0.0/24"));
    return scan;
  }
}
