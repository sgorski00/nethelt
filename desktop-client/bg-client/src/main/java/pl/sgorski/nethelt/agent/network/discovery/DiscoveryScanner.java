package pl.sgorski.nethelt.agent.network.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.discovery.DiscoveredDevice;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveryScanner {

  private final HostProbe hostProbe;
  private final DiscoveryProperties discoveryProperties;

  public List<DiscoveredDevice> scan(IpRange range) {
    log.info(
        "Scanning {} addresses from {} to {}",
        range.size(),
        range.startAddress().getHostAddress(),
        range.endAddress().getHostAddress());
    var permits = new Semaphore(discoveryProperties.maxParallelProbes());
    List<Future<Optional<DiscoveredDevice>>> probes;
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      probes =
          range.addresses().stream()
              .map(address -> executor.submit(() -> probe(address, permits)))
              .toList();
    }
    try {
      var devices = probes.stream().flatMap(probe -> probe.resultNow().stream()).toList();
      log.info("Scan finished, found {} devices", devices.size());
      return devices;
    } catch (IllegalStateException e) {
      throw new NetworkException("Discovery scan was interrupted", e);
    }
  }

  private Optional<DiscoveredDevice> probe(Inet4Address address, Semaphore permits)
      throws InterruptedException {
    permits.acquire();
    try {
      var startTime = System.nanoTime();
      if (!hostProbe.isReachable(address, discoveryProperties.pingTimeout())) {
        return Optional.empty();
      }
      var responseTimeMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
      var hostname = hostProbe.resolveHostname(address);
      return Optional.of(new DiscoveredDevice(address, hostname, responseTimeMs));
    } catch (IOException e) {
      log.debug("Probing {} failed: {}", address.getHostAddress(), e.getMessage());
      return Optional.empty();
    } finally {
      permits.release();
    }
  }
}
