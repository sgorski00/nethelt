package pl.sgorski.nethelt.webapi.features.discovery.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

public class DiscoveryScanMapperTests {

  private final DiscoveryScanMapper mapper = new DiscoveryScanMapperImpl();

  @Test
  void toResponse_shouldMarkResultsOfExistingDevices() {
    var scan = new DiscoveryScan(TestNetworkFactory.createNetwork(), null);
    scan.start(Ipv4Range.parse("192.168.0.0/24"));
    scan.addResult(Inet4Address.ofLiteral("192.168.0.1"), "router", 1);
    scan.addResult(Inet4Address.ofLiteral("192.168.0.20"), null, 5);
    var router = TestDeviceFactory.createDevice("Router", "192.168.0.1");
    var other = TestDeviceFactory.createDevice("Other", "192.168.0.30");
    ReflectionTestUtils.setField(router, "id", 10L);
    ReflectionTestUtils.setField(other, "id", 30L);

    var response = mapper.toResponse(scan, Set.of(router, other));

    assertEquals("192.168.0.0", response.rangeStart());
    assertEquals("192.168.0.255", response.rangeEnd());
    assertEquals(
        List.of("192.168.0.1", "192.168.0.20"),
        response.results().stream().map(r -> r.ipAddress()).toList());
    assertEquals(10L, response.results().get(0).existingDeviceId());
    assertNull(response.results().get(1).existingDeviceId());
  }
}
