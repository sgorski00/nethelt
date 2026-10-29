package pl.sgorski.nethelt.webapi.features.discovery.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanValidationFailedException;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentWebService;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanStartCommand;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Service
@RequiredArgsConstructor
public class DiscoveryScanWebService {

  private final DiscoveryScanRepository discoveryScanRepository;
  private final AgentWebService agentWebService;
  private final DiscoveryScanProperties discoveryScanProperties;

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
      throw new DiscoveryScanNotAllowedException("the agent must be active and online");
    }
    var existingScan = discoveryScanRepository.findByNetworkIdForUpdate(command.networkId());
    if (existingScan.isEmpty()) {
      return createScan(agent.getNetwork(), range, now);
    }
    var scan = existingScan.get();
    scan.timeOutIfExpired(now, discoveryScanProperties.timeout());
    if (scan.isInProgress()) {
      throw new DiscoveryScanNotAllowedException("another scan is already in progress");
    }
    scan.restart(range, now);
    return scan;
  }

  private DiscoveryScan createScan(Network network, @Nullable Ipv4Range range, Instant now) {
    try {
      return discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, range, now));
    } catch (DataIntegrityViolationException e) {
      throw new DiscoveryScanNotAllowedException("another scan is already in progress");
    }
  }

  private @Nullable Ipv4Range parseRange(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    var range = Ipv4Range.parse(value);
    if (range.size() > DiscoveryScan.MAX_RANGE_SIZE) {
      throw new DiscoveryScanValidationFailedException(
          "the IP range can have at most " + DiscoveryScan.MAX_RANGE_SIZE + " addresses (/24)");
    }
    return range;
  }
}
