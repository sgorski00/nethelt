package pl.sgorski.nethelt.webapi.features.discovery.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

public class DiscoveryScanTests {

  private static final Duration TIMEOUT = Duration.ofMinutes(10);
  private final Instant now = Instant.parse("2026-10-29T12:00:00Z");

  @Test
  void constructor_shouldCreatePendingScan() {
    var network = TestNetworkFactory.createNetwork();
    var range = Ipv4Range.parse("192.168.0.0/24");

    var scan = new DiscoveryScan(network, range, now);

    assertSame(network, scan.getNetwork());
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertEquals(range, scan.getRange());
    assertEquals(now, scan.getRequestedAt());
    assertNull(scan.getStartedAt());
    assertNull(scan.getFinishedAt());
    assertNull(scan.getFailureReason());
    assertTrue(scan.isInProgress());
  }

  @Test
  void constructor_shouldLeaveRangeEmpty_whenNotSet() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null, now);

    assertNull(scan.getRange());
    assertNull(scan.getRangeStart());
    assertNull(scan.getRangeEnd());
  }

  @Test
  void restart_shouldResetPreviousScan() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null, now);
    ReflectionTestUtils.setField(scan, "status", DiscoveryScanStatus.FAILED);
    ReflectionTestUtils.setField(scan, "startedAt", now.plusSeconds(5));
    ReflectionTestUtils.setField(scan, "finishedAt", now.plusSeconds(30));
    ReflectionTestUtils.setField(scan, "failureReason", "No network interface");
    var range = Ipv4Range.parse("10.0.0.0/24");
    var restartedAt = now.plus(Duration.ofHours(1));

    scan.restart(range, restartedAt);

    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertEquals(range, scan.getRange());
    assertEquals(restartedAt, scan.getRequestedAt());
    assertNull(scan.getStartedAt());
    assertNull(scan.getFinishedAt());
    assertNull(scan.getFailureReason());
  }

  @Test
  void isInProgress_shouldReturnTrue_whenRunning() {
    var scan = scanWithStatus(DiscoveryScanStatus.RUNNING);

    assertTrue(scan.isInProgress());
  }

  @Test
  void isInProgress_shouldReturnFalse_whenFinished() {
    for (var status :
        new DiscoveryScanStatus[] {
          DiscoveryScanStatus.COMPLETED, DiscoveryScanStatus.FAILED, DiscoveryScanStatus.TIMED_OUT
        }) {
      assertFalse(scanWithStatus(status).isInProgress(), status.name());
    }
  }

  @Test
  void timeOutIfExpired_shouldTimeOut_whenInProgressLongerThanTimeout() {
    var scan = scanWithStatus(DiscoveryScanStatus.RUNNING);
    var later = now.plus(TIMEOUT).plusSeconds(1);

    assertTrue(scan.timeOutIfExpired(later, TIMEOUT));
    assertEquals(DiscoveryScanStatus.TIMED_OUT, scan.getStatus());
    assertEquals(later, scan.getFinishedAt());
    assertFalse(scan.isInProgress());
  }

  @Test
  void timeOutIfExpired_shouldDoNothing_whenWithinTimeout() {
    var scan = scanWithStatus(DiscoveryScanStatus.PENDING);

    assertFalse(scan.timeOutIfExpired(now.plus(TIMEOUT), TIMEOUT));
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertNull(scan.getFinishedAt());
  }

  @Test
  void timeOutIfExpired_shouldDoNothing_whenFinished() {
    var scan = scanWithStatus(DiscoveryScanStatus.COMPLETED);

    assertFalse(scan.timeOutIfExpired(now.plus(Duration.ofDays(1)), TIMEOUT));
    assertEquals(DiscoveryScanStatus.COMPLETED, scan.getStatus());
  }

  private DiscoveryScan scanWithStatus(DiscoveryScanStatus status) {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null, now);
    ReflectionTestUtils.setField(scan, "status", status);
    return scan;
  }
}
