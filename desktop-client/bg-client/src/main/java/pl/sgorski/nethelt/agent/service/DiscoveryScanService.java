package pl.sgorski.nethelt.agent.service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.mapper.DiscoveryScanMapper;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;
import pl.sgorski.nethelt.agent.network.discovery.DiscoveryScanner;
import pl.sgorski.nethelt.agent.network.discovery.LocalSubnetResolver;
import pl.sgorski.nethelt.agent.webclient.api.web.DiscoveryScanClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanFailureRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.DiscoveryScanResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryScanService {

  private final DiscoveryScanClient discoveryScanClient;
  private final DiscoveryScanMapper discoveryScanMapper;
  private final LocalSubnetResolver localSubnetResolver;
  private final DiscoveryScanner discoveryScanner;
  private final AtomicBoolean running = new AtomicBoolean();

  public void startPendingScan() {
    if (running.get()) {
      log.debug("Discovery scan is already running, skipping poll");
      return;
    }
    discoveryScanClient.fetchPendingScan().flatMap(this::resolveRange).ifPresent(this::startScan);
  }

  private Optional<IpRange> resolveRange(DiscoveryScanResponse pendingScan) {
    try {
      var range = discoveryScanMapper.toRange(pendingScan);
      return Optional.of(range != null ? range : localSubnetResolver.resolve());
    } catch (NetworkException | IllegalArgumentException e) {
      log.error("Could not determine the range to scan: {}", e.getMessage(), e);
      reportFailure(e);
      return Optional.empty();
    }
  }

  private void startScan(IpRange range) {
    discoveryScanClient.acknowledgeScan(discoveryScanMapper.toAckRequest(range));
    running.set(true);
    Thread.ofVirtual().name("nethelt-discovery-scan").start(() -> runScan(range));
  }

  private void runScan(IpRange range) {
    try {
      var devices = discoveryScanner.scan(range);
      discoveryScanClient.sendResults(discoveryScanMapper.toResultsRequest(devices));
      log.info("Discovery scan results sent, found {} devices", devices.size());
    } catch (Exception e) {
      log.error("Discovery scan failed: {}", e.getMessage(), e);
      reportFailure(e);
    } finally {
      running.set(false);
    }
  }

  private void reportFailure(Exception cause) {
    try {
      var reason = Objects.requireNonNullElse(cause.getMessage(), cause.getClass().getSimpleName());
      discoveryScanClient.reportFailure(new DiscoveryScanFailureRequest(reason));
    } catch (Exception e) {
      log.warn("Could not report discovery scan failure: {}", e.getMessage());
    }
  }
}
