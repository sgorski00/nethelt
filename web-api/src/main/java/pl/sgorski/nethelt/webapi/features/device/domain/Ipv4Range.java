package pl.sgorski.nethelt.webapi.features.device.domain;

import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;

/**
 * Inclusive range of IPv4 addresses stored as unsigned 32-bit numbers, the same way as {@link
 * Device#getIpNumeric()}.
 */
public record Ipv4Range(long start, long end) {

  public static final int MAX_RANGES = 20;

  private static final String OCTET = "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
  private static final Pattern IPV4 = Pattern.compile(OCTET + "(\\." + OCTET + "){3}");
  private static final Pattern PREFIX = Pattern.compile("\\d{1,2}");

  /** Parses a comma-separated filter, e.g. {@code 10.0.0.5, 192.168.0.0/24, 10.0.0.1-10.0.0.50}. */
  public static List<Ipv4Range> parseAll(String filter) {
    var values = filter.split(",", -1);
    if (values.length > MAX_RANGES) {
      throw invalid(filter, "at most " + MAX_RANGES + " values are allowed");
    }
    return Arrays.stream(values).map(Ipv4Range::parse).toList();
  }

  /** Parses a single address, a subnet with a prefix from 0 to 32 or a range of two addresses. */
  public static Ipv4Range parse(String value) {
    value = value.strip();
    if (value.contains("-")) return parseRange(value);
    if (value.contains("/")) return parseSubnet(value);
    var address = toNumber(value, value);
    return new Ipv4Range(address, address);
  }

  public static long toNumber(Inet4Address address) {
    return Integer.toUnsignedLong(ByteBuffer.wrap(address.getAddress()).getInt());
  }

  private static Ipv4Range parseRange(String value) {
    var parts = value.split("-", -1);
    if (parts.length != 2) throw invalid(value, "range must have exactly one '-'");
    var start = toNumber(parts[0].strip(), value);
    var end = toNumber(parts[1].strip(), value);
    if (start > end) throw invalid(value, "range start is greater than its end");
    return new Ipv4Range(start, end);
  }

  private static Ipv4Range parseSubnet(String value) {
    var parts = value.split("/", -1);
    if (parts.length != 2) throw invalid(value, "subnet must have exactly one '/'");
    var address = toNumber(parts[0].strip(), value);
    var hostMask = (1L << (32 - toPrefix(parts[1].strip(), value))) - 1;
    return new Ipv4Range(address & ~hostMask, address | hostMask);
  }

  private static long toNumber(String address, String value) {
    if (!IPV4.matcher(address).matches()) throw invalid(value, "invalid IP address");
    return toNumber(Inet4Address.ofLiteral(address));
  }

  private static int toPrefix(String prefix, String value) {
    if (!PREFIX.matcher(prefix).matches() || Integer.parseInt(prefix) > 32) {
      throw invalid(value, "subnet mask must be a number from 0 to 32");
    }
    return Integer.parseInt(prefix);
  }

  private static DeviceValidationFailedException invalid(String value, String reason) {
    return new DeviceValidationFailedException("invalid IP filter '" + value + "': " + reason);
  }
}
