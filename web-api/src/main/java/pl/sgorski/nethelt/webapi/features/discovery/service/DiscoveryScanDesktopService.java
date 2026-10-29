package pl.sgorski.nethelt.webapi.features.discovery.service;

import java.net.Inet4Address;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanAckCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanCompleteCommand;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;

@Service
@RequiredArgsConstructor
public class DiscoveryScanDesktopService {

  private final DiscoveryScanRepository discoveryScanRepository;
  private final DiscoveryScanProperties discoveryScanProperties;

  public Optional<DiscoveryScan> getPendingScan(Long networkId) {
    return discoveryScanRepository
        .findByNetworkId(networkId)
        .filter(scan -> scan.getStatus() == DiscoveryScanStatus.PENDING)
        .filter(scan -> !scan.isExpired(Instant.now(), discoveryScanProperties.timeout()));
  }

  @Transactional
  public void acknowledgeScan(DiscoveryScanAckCommand command) {
    var range = Ipv4Range.of(command.rangeStart(), command.rangeEnd());
    var scan = getScanForUpdate(command.networkId());
    scan.start(range);
  }

  @Transactional
  public void completeScan(DiscoveryScanCompleteCommand command) {
    var scan = getScanForUpdate(command.networkId());
    command
        .results()
        .forEach(
            result ->
                scan.addResult(
                    parseIpAddress(result.ipAddress()),
                    result.hostname(),
                    result.responseTimeMs()));
    scan.complete();
  }

  @Transactional
  public void failScan(Long networkId, String reason) {
    getScanForUpdate(networkId).fail(reason);
  }

  private DiscoveryScan getScanForUpdate(Long networkId) {
    var scan =
        discoveryScanRepository
            .findByNetworkIdForUpdate(networkId)
            .orElseThrow(DiscoveryScanNotFoundException::new);
    if (scan.isExpired(Instant.now(), discoveryScanProperties.timeout())) {
      throw new DiscoveryScanNotAllowedException("The discovery scan has timed out");
    }
    return scan;
  }

  private static Inet4Address parseIpAddress(String ipAddress) {
    try {
      return Inet4Address.ofLiteral(ipAddress);
    } catch (IllegalArgumentException e) {
      throw new DiscoveryScanValidationFailedException("invalid IP address: " + ipAddress);
    }
  }
}
