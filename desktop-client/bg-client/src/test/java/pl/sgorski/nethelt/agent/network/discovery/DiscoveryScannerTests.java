package pl.sgorski.nethelt.agent.network.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.discovery.DiscoveredDevice;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;

public class DiscoveryScannerTests {

  private static final DiscoveryProperties PROPERTIES =
      new DiscoveryProperties(Duration.ofMillis(100), 4);

  @Test
  void scan_shouldReturnReachableHostsInAddressOrder() {
    var probe =
        new FakeHostProbe(
            Set.of("192.168.0.20", "192.168.0.1", "192.168.0.7"),
            Map.of("192.168.0.1", "router.lan"));
    var scanner = new DiscoveryScanner(probe, PROPERTIES);

    var devices = scanner.scan(range("192.168.0.1", "192.168.0.30"));

    assertEquals(
        List.of(ip("192.168.0.1"), ip("192.168.0.7"), ip("192.168.0.20")),
        devices.stream().map(DiscoveredDevice::ipAddress).toList());
    assertEquals("router.lan", devices.getFirst().hostname());
    assertNull(devices.get(1).hostname());
    assertTrue(devices.stream().allMatch(device -> device.responseTimeMs() >= 0));
  }

  @Test
  void scan_shouldReturnEmptyList_whenNoHostIsReachable() {
    var scanner = new DiscoveryScanner(new FakeHostProbe(Set.of(), Map.of()), PROPERTIES);

    assertTrue(scanner.scan(range("192.168.0.1", "192.168.0.10")).isEmpty());
  }

  @Test
  void scan_shouldSkipHosts_whenProbeThrows() {
    var probe =
        new FakeHostProbe(Set.of("192.168.0.1", "192.168.0.2"), Map.of()) {
          @Override
          public boolean isReachable(Inet4Address address, Duration timeout) throws IOException {
            if (address.equals(ip("192.168.0.1"))) {
              throw new IOException("Permission denied");
            }
            return super.isReachable(address, timeout);
          }
        };
    var scanner = new DiscoveryScanner(probe, PROPERTIES);

    var devices = scanner.scan(range("192.168.0.1", "192.168.0.2"));

    assertEquals(
        List.of(ip("192.168.0.2")), devices.stream().map(DiscoveredDevice::ipAddress).toList());
  }

  @Test
  void scan_shouldPassPingTimeoutToProbe() {
    var probe = new FakeHostProbe(Set.of("192.168.0.1"), Map.of());
    var scanner = new DiscoveryScanner(probe, PROPERTIES);

    scanner.scan(range("192.168.0.1", "192.168.0.1"));

    assertEquals(PROPERTIES.pingTimeout(), probe.lastTimeout);
  }

  @Test
  void scan_shouldLimitParallelProbes() {
    var probe = new FakeHostProbe(Set.of(), Map.of());
    var scanner = new DiscoveryScanner(probe, PROPERTIES);

    scanner.scan(range("192.168.0.1", "192.168.0.64"));

    assertTrue(probe.maxConcurrent.get() <= PROPERTIES.maxParallelProbes());
    assertEquals(64, probe.calls.get());
  }

  private static IpRange range(String start, String end) {
    return IpRange.of(ip(start), ip(end));
  }

  private static Inet4Address ip(String address) {
    return Inet4Address.ofLiteral(address);
  }

  private static class FakeHostProbe implements HostProbe {

    private final Set<String> reachable;
    private final Map<String, String> hostnames;
    private final AtomicInteger concurrent = new AtomicInteger();
    final AtomicInteger maxConcurrent = new AtomicInteger();
    final AtomicInteger calls = new AtomicInteger();
    volatile @Nullable Duration lastTimeout;

    FakeHostProbe(Set<String> reachable, Map<String, String> hostnames) {
      this.reachable = reachable;
      this.hostnames = hostnames;
    }

    @Override
    public boolean isReachable(Inet4Address address, Duration timeout) throws IOException {
      calls.incrementAndGet();
      lastTimeout = timeout;
      maxConcurrent.accumulateAndGet(concurrent.incrementAndGet(), Math::max);
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        concurrent.decrementAndGet();
      }
      return reachable.contains(address.getHostAddress());
    }

    @Override
    public @Nullable String resolveHostname(Inet4Address address) {
      return hostnames.get(address.getHostAddress());
    }
  }
}
