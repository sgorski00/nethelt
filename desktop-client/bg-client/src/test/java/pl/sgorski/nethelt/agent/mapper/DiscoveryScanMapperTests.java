package pl.sgorski.nethelt.agent.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.discovery.DiscoveredDevice;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.DiscoveryScanResponse;

public class DiscoveryScanMapperTests {

  private final DiscoveryScanMapper mapper = new DiscoveryScanMapper();

  @Test
  void toRange_shouldReturnRange_whenSet() {
    var range = mapper.toRange(new DiscoveryScanResponse("192.168.0.10", "192.168.0.20"));

    assertEquals(IpRange.of(ip("192.168.0.10"), ip("192.168.0.20")), range);
  }

  @Test
  void toRange_shouldReturnNull_whenNotSet() {
    assertNull(mapper.toRange(new DiscoveryScanResponse(null, null)));
  }

  @Test
  void toRange_shouldThrow_whenAddressIsInvalid() {
    var response = new DiscoveryScanResponse("192.168.0.300", "192.168.0.20");

    assertThrows(IllegalArgumentException.class, () -> mapper.toRange(response));
  }

  @Test
  void toAckRequest_shouldFormatRange() {
    var request = mapper.toAckRequest(IpRange.of(ip("10.0.0.1"), ip("10.0.0.254")));

    assertEquals(new DiscoveryScanAckRequest("10.0.0.1", "10.0.0.254"), request);
  }

  @Test
  void toResultsRequest_shouldMapDevices() {
    var request =
        mapper.toResultsRequest(
            List.of(
                new DiscoveredDevice(ip("10.0.0.1"), "router.lan", 2),
                new DiscoveredDevice(ip("10.0.0.7"), null, 15)));

    assertEquals(
        List.of(
            new DiscoveryScanResultRequest("10.0.0.1", "router.lan", 2),
            new DiscoveryScanResultRequest("10.0.0.7", null, 15)),
        request.results());
  }

  private static Inet4Address ip(String address) {
    return Inet4Address.ofLiteral(address);
  }
}
