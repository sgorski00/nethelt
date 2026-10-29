package pl.sgorski.nethelt.webapi.features.discovery.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotFoundException;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentWebService;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanStartCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Service
@RequiredArgsConstructor
public class DiscoveryScanWebService {

  private final DiscoveryScanRepository discoveryScanRepository;
  private final AgentWebService agentWebService;
  private final DiscoveryScanProperties discoveryScanProperties;
  private final ApplicationEventPublisher eventPublisher;

  public DiscoveryScan getScan(Long networkId) {
    return discoveryScanRepository
        .findByNetworkId(networkId)
        .orElseThrow(DiscoveryScanNotFoundException::new);
  }

  @Transactional
  public DiscoveryScan startScan(DiscoveryScanStartCommand command) {
    var range = parseRange(command.ipRange());
    var agent = agentWebService.getAgent(command.networkId());
    var now = Instant.now();
    if (!agent.isActive() || agent.connectionState(now) != AgentConnectionState.ONLINE) {
      throw new DiscoveryScanNotAllowedException(
          "Cannot start discovery scan: the agent must be active and online");
    }
    var scan =
        discoveryScanRepository
            .findByNetworkIdForUpdate(command.networkId())
            .map(existingScan -> restartScan(existingScan, range, now))
            .orElseGet(() -> createScan(agent.getNetwork(), range));
    eventPublisher.publishEvent(DiscoveryScanStatusChangedEvent.of(scan));
    return scan;
  }

  private DiscoveryScan restartScan(DiscoveryScan scan, @Nullable Ipv4Range range, Instant now) {
    if (scan.timeOutIfExpired(now, discoveryScanProperties.timeout())) {
      eventPublisher.publishEvent(DiscoveryScanStatusChangedEvent.of(scan));
    }
    if (scan.isInProgress()) {
      throw new DiscoveryScanNotAllowedException(
          "Cannot start discovery scan: another scan is already in progress");
    }
    scan.restart(range);
    return scan;
  }

  private DiscoveryScan createScan(Network network, @Nullable Ipv4Range range) {
    try {
      return discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, range));
    } catch (DataIntegrityViolationException e) {
      throw new DiscoveryScanNotAllowedException(
          "Cannot start discovery scan: another scan is already in progress");
    }
  }

  private @Nullable Ipv4Range parseRange(@Nullable String value) {
    return value == null || value.isBlank() ? null : Ipv4Range.parse(value);
  }
}
