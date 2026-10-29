package pl.sgorski.nethelt.webapi.features.discovery.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

public class DiscoveryScanTests {

  private static final Duration TIMEOUT = Duration.ofMinutes(10);
  private final Instant now = Instant.parse("2020-01-01T12:00:00Z");

  @Test
  void constructor_shouldCreatePendingScan() {
    var network = TestNetworkFactory.createNetwork();
    var range = Ipv4Range.parse("192.168.0.0/24");

    var scan = new DiscoveryScan(network, range);

    assertSame(network, scan.getNetwork());
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertEquals(range, scan.getRange());
    assertNotNull(scan.getRequestedAt());
    assertNull(scan.getStartedAt());
    assertNull(scan.getFinishedAt());
    assertNull(scan.getFailureReason());
    assertTrue(scan.isInProgress());
  }

  @Test
  void constructor_shouldLeaveRangeEmpty_whenNotSet() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);

    assertNull(scan.getRange());
    assertNull(scan.getRangeStart());
    assertNull(scan.getRangeEnd());
  }

  @Test
  void restart_shouldResetPreviousScan() {
    var scan = scanWithStatus(DiscoveryScanStatus.FAILED);
    ReflectionTestUtils.setField(scan, "status", DiscoveryScanStatus.FAILED);
    ReflectionTestUtils.setField(scan, "startedAt", now.plusSeconds(5));
    ReflectionTestUtils.setField(scan, "finishedAt", now.plusSeconds(30));
    ReflectionTestUtils.setField(scan, "failureReason", "No network interface");
    var range = Ipv4Range.parse("10.0.0.0/24");

    scan.restart(range);

    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertEquals(range, scan.getRange());
    assertTrue(scan.getRequestedAt().isAfter(now));
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

  @Test
  void constructor_shouldThrow_whenRangeIsLargerThan24() {
    var network = TestNetworkFactory.createNetwork();
    var range = Ipv4Range.parse("192.168.0.0/23");

    assertThrows(
        DiscoveryScanValidationFailedException.class, () -> new DiscoveryScan(network, range));
  }

  @Test
  void restart_shouldClearResults() {
    var scan = runningScan();
    scan.addResult(ip("192.168.0.1"), "router", 2);
    scan.complete();

    scan.restart(null);

    assertTrue(scan.getResults().isEmpty());
  }

  @Test
  void isExpired_shouldReturnTrue_whenInProgressLongerThanTimeout() {
    var scan = scanWithStatus(DiscoveryScanStatus.RUNNING);

    assertTrue(scan.isExpired(now.plus(TIMEOUT).plusSeconds(1), TIMEOUT));
    assertFalse(scan.isExpired(now.plus(TIMEOUT), TIMEOUT));
  }

  @Test
  void isExpired_shouldReturnFalse_whenFinished() {
    var scan = scanWithStatus(DiscoveryScanStatus.COMPLETED);

    assertFalse(scan.isExpired(now.plus(Duration.ofDays(1)), TIMEOUT));
  }

  @Test
  void start_shouldMarkScanAsRunningWithRange() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);
    var range = Ipv4Range.parse("192.168.0.0/24");

    scan.start(range);

    assertEquals(DiscoveryScanStatus.RUNNING, scan.getStatus());
    assertEquals(range, scan.getRange());
    assertNotNull(scan.getStartedAt());
  }

  @Test
  void start_shouldThrow_whenNotPending() {
    var scan = scanWithStatus(DiscoveryScanStatus.RUNNING);
    var range = Ipv4Range.parse("192.168.0.0/24");

    assertThrows(DiscoveryScanNotAllowedException.class, () -> scan.start(range));
  }

  @Test
  void start_shouldThrow_whenRangeIsLargerThan24() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);
    var range = Ipv4Range.parse("10.0.0.0/16");

    assertThrows(DiscoveryScanValidationFailedException.class, () -> scan.start(range));
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
  }

  @Test
  void addResult_shouldAddResult() {
    var scan = runningScan();

    scan.addResult(ip("192.168.0.20"), "laptop.lan", 4);

    var result = scan.getResults().getFirst();
    assertSame(scan, result.getScan());
    assertEquals(ip("192.168.0.20"), result.getIpAddress());
    assertEquals("laptop.lan", result.getHostname());
    assertEquals(4, result.getResponseTimeMs());
  }

  @Test
  void addResult_shouldThrow_whenAddressIsOutsideOfRange() {
    var scan = runningScan();

    assertThrows(
        DiscoveryScanValidationFailedException.class,
        () -> scan.addResult(ip("192.168.1.1"), null, 1));
  }

  @Test
  void addResult_shouldThrow_whenAddressIsReportedTwice() {
    var scan = runningScan();
    scan.addResult(ip("192.168.0.1"), null, 1);

    assertThrows(
        DiscoveryScanValidationFailedException.class,
        () -> scan.addResult(ip("192.168.0.1"), null, 1));
  }

  @Test
  void addResult_shouldThrow_whenNotRunning() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);

    assertThrows(
        DiscoveryScanNotAllowedException.class, () -> scan.addResult(ip("192.168.0.1"), null, 1));
  }

  @Test
  void complete_shouldMarkScanAsCompleted() {
    var scan = runningScan();

    scan.complete();

    assertEquals(DiscoveryScanStatus.COMPLETED, scan.getStatus());
    assertNotNull(scan.getFinishedAt());
  }

  @Test
  void complete_shouldThrow_whenNotRunning() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);

    assertThrows(DiscoveryScanNotAllowedException.class, () -> scan.complete());
  }

  @Test
  void fail_shouldMarkScanAsFailed() {
    for (var status :
        new DiscoveryScanStatus[] {DiscoveryScanStatus.PENDING, DiscoveryScanStatus.RUNNING}) {
      var scan = scanWithStatus(status);

      scan.fail("No network interface");

      assertEquals(DiscoveryScanStatus.FAILED, scan.getStatus());
      assertEquals("No network interface", scan.getFailureReason());
      assertNotNull(scan.getFinishedAt());
    }
  }

  @Test
  void fail_shouldTruncateLongReason() {
    var scan = runningScan();

    scan.fail("x".repeat(300));

    assertEquals(255, scan.getFailureReason().length());
  }

  @Test
  void fail_shouldThrow_whenFinished() {
    var scan = scanWithStatus(DiscoveryScanStatus.COMPLETED);

    assertThrows(DiscoveryScanNotAllowedException.class, () -> scan.fail("error"));
  }

  private DiscoveryScan runningScan() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);
    scan.start(Ipv4Range.parse("192.168.0.0/24"));
    return scan;
  }

  private static Inet4Address ip(String address) {
    return Inet4Address.ofLiteral(address);
  }

  private DiscoveryScan scanWithStatus(DiscoveryScanStatus status) {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);
    ReflectionTestUtils.setField(scan, "requestedAt", now);
    ReflectionTestUtils.setField(scan, "status", status);
    return scan;
  }
}
