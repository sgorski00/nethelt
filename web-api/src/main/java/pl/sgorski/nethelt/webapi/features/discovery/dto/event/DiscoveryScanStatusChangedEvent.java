package pl.sgorski.nethelt.webapi.features.discovery.dto.event;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;

public record DiscoveryScanStatusChangedEvent(
    Long userId,
    Long networkId,
    String networkName,
    DiscoveryScanStatus status,
    int resultCount,
    @Nullable String failureReason) {

  public static DiscoveryScanStatusChangedEvent of(DiscoveryScan scan) {
    var network = scan.getNetwork();
    return new DiscoveryScanStatusChangedEvent(
        network.getUser().getId(),
        network.getId(),
        network.getName(),
        scan.getStatus(),
        scan.getResults().size(),
        scan.getFailureReason());
  }
}
