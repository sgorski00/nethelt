package pl.sgorski.nethelt.webapi.features.device.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet4Address;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.sgorski.nethelt.webapi.exception.domain.device.InvalidIpRangeException;

public class Ipv4RangeTests {

  @ParameterizedTest
  @CsvSource({
    "10.0.0.5, 10.0.0.5, 10.0.0.5",
    "' 10.0.0.5 ', 10.0.0.5, 10.0.0.5",
    "192.168.0.0/24, 192.168.0.0, 192.168.0.255",
    "192.168.0.77/24, 192.168.0.0, 192.168.0.255",
    "10.1.2.3/32, 10.1.2.3, 10.1.2.3",
    "10.1.2.3/0, 0.0.0.0, 255.255.255.255",
    "172.16.0.0/12, 172.16.0.0, 172.31.255.255",
    "10.0.0.1-10.0.0.50, 10.0.0.1, 10.0.0.50",
    "'10.0.0.1 - 10.0.0.1', 10.0.0.1, 10.0.0.1",
  })
  void parse_shouldReturnRange_whenValueIsValid(String value, String start, String end) {
    var range = Ipv4Range.parse(value);

    assertEquals(toNumber(start), range.start());
    assertEquals(toNumber(end), range.end());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "10.0.0",
        "10.1",
        "010.0.0.1",
        "10.0.0.256",
        "10.0.0.1/33",
        "10.0.0.1/-1",
        "10.0.0.1/255.255.255.0",
        "10.0.0.1/",
        "10.0.0.1/24/8",
        "10.0.0.50-10.0.0.1",
        "10.0.0.1-",
        "10.0.0.1-10.0.0.2-10.0.0.3",
        "abc"
      })
  void parse_shouldThrow_whenValueIsInvalid(String value) {
    assertThrows(InvalidIpRangeException.class, () -> Ipv4Range.parse(value));
  }

  @Test
  void parseAll_shouldReturnRangeForEachValue() {
    var ranges = Ipv4Range.parseAll("192.168.1.2, 10.0.0.0/24,172.16.0.1-172.16.0.9");

    assertEquals(
        List.of(
            Ipv4Range.parse("192.168.1.2"),
            Ipv4Range.parse("10.0.0.0/24"),
            Ipv4Range.parse("172.16.0.1-172.16.0.9")),
        ranges);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "10.0.0.1,", "10.0.0.1,,10.0.0.2", "10.0.0.1, 10.0.0.300"})
  void parseAll_shouldThrow_whenAnyValueIsInvalid(String value) {
    assertThrows(InvalidIpRangeException.class, () -> Ipv4Range.parseAll(value));
  }

  @Test
  void parseAll_shouldThrow_whenTooManyValues() {
    var value =
        IntStream.rangeClosed(0, Ipv4Range.MAX_RANGES)
            .mapToObj(i -> "10.0.0." + i)
            .collect(Collectors.joining(","));

    assertThrows(InvalidIpRangeException.class, () -> Ipv4Range.parseAll(value));
  }

  @ParameterizedTest
  @CsvSource({
    "10.0.0.5, 1",
    "192.168.0.0/24, 256",
    "10.0.0.1-10.0.0.50, 50",
    "0.0.0.0/0, 4294967296",
  })
  void size_shouldReturnNumberOfAddresses(String value, long size) {
    assertEquals(size, Ipv4Range.parse(value).size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0.0.0.0", "10.0.0.1", "192.168.0.255", "255.255.255.255"})
  void toLiteral_shouldFormatAddress(String address) {
    assertEquals(address, Ipv4Range.toLiteral(toNumber(address)));
  }

  private static long toNumber(String address) {
    return Ipv4Range.toNumber(Inet4Address.ofLiteral(address));
  }
}
