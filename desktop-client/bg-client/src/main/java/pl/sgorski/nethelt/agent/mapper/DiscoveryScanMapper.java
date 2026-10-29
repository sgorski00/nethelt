package pl.sgorski.nethelt.agent.mapper;

import java.net.Inet4Address;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.model.discovery.DiscoveredDevice;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultsRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.DiscoveryScanResponse;

@Component
public class DiscoveryScanMapper {

  public @Nullable IpRange toRange(DiscoveryScanResponse response) {
    if (response.rangeStart() == null || response.rangeEnd() == null) {
      return null;
    }
    return IpRange.of(
        Inet4Address.ofLiteral(response.rangeStart()), Inet4Address.ofLiteral(response.rangeEnd()));
  }

  public DiscoveryScanAckRequest toAckRequest(IpRange range) {
    return new DiscoveryScanAckRequest(
        range.startAddress().getHostAddress(), range.endAddress().getHostAddress());
  }

  public DiscoveryScanResultsRequest toResultsRequest(List<DiscoveredDevice> devices) {
    return new DiscoveryScanResultsRequest(
        devices.stream()
            .map(
                device ->
                    new DiscoveryScanResultRequest(
                        device.ipAddress().getHostAddress(),
                        device.hostname(),
                        device.responseTimeMs()))
            .toList());
  }
}
