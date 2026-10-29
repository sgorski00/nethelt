package pl.sgorski.nethelt.agent.model.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class IpRangeTests {

  @ParameterizedTest
  @CsvSource({
    "192.168.1.77, 24, 192.168.1.1, 192.168.1.254",
    "10.20.30.40, 8, 10.20.30.1, 10.20.30.254",
    "172.16.5.9, 16, 172.16.5.1, 172.16.5.254",
    "192.168.1.77, 26, 192.168.1.65, 192.168.1.126",
    "192.168.1.6, 30, 192.168.1.5, 192.168.1.6",
    "192.168.1.6, 31, 192.168.1.6, 192.168.1.7",
    "192.168.1.6, 32, 192.168.1.6, 192.168.1.6",
  })
  void hostsOf_shouldReturnHostsOfSubnetNarrowedTo24(
      String address, int prefixLength, String start, String end) {
    var range = IpRange.hostsOf(ip(address), prefixLength);

    assertEquals(ip(start), range.startAddress());
    assertEquals(ip(end), range.endAddress());
  }

  @Test
  void of_shouldCreateRangeBetweenAddresses() {
    var range = IpRange.of(ip("10.0.0.250"), ip("10.0.1.5"));

    assertEquals(12, range.size());
    assertEquals(ip("10.0.0.250"), range.startAddress());
    assertEquals(ip("10.0.1.5"), range.endAddress());
  }

  @Test
  void of_shouldThrow_whenStartIsAfterEnd() {
    assertThrows(IllegalArgumentException.class, () -> IpRange.of(ip("10.0.0.2"), ip("10.0.0.1")));
  }

  @Test
  void addresses_shouldReturnEveryAddressInOrder() {
    var range = IpRange.of(ip("192.168.0.254"), ip("192.168.1.1"));

    assertEquals(
        List.of(ip("192.168.0.254"), ip("192.168.0.255"), ip("192.168.1.0"), ip("192.168.1.1")),
        range.addresses());
  }

  @Test
  void addresses_shouldHandleHighestAddresses() {
    var range = IpRange.of(ip("255.255.255.254"), ip("255.255.255.255"));

    assertEquals(List.of(ip("255.255.255.254"), ip("255.255.255.255")), range.addresses());
  }

  private static Inet4Address ip(String address) {
    return Inet4Address.ofLiteral(address);
  }
}
