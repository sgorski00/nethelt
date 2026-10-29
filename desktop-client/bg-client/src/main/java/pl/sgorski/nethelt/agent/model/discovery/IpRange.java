package pl.sgorski.nethelt.agent.model.discovery;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.LongStream;

public record IpRange(long start, long end) {

  public static final int MIN_PREFIX_LENGTH = 24;

  public IpRange {
    if (start < 0 || end > 0xFFFFFFFFL || start > end) {
      throw new IllegalArgumentException("Invalid IP range: " + start + "-" + end);
    }
  }

  public static IpRange of(Inet4Address start, Inet4Address end) {
    return new IpRange(toNumber(start), toNumber(end));
  }

  /** Subnets larger than /24 are narrowed to the /24 around {@code address}. */
  public static IpRange hostsOf(Inet4Address address, int prefixLength) {
    var prefix = Math.clamp(prefixLength, MIN_PREFIX_LENGTH, 32);
    var hostMask = (1L << (32 - prefix)) - 1;
    var network = toNumber(address) & ~hostMask;
    var broadcast = network | hostMask;
    return prefix >= 31 ? new IpRange(network, broadcast) : new IpRange(network + 1, broadcast - 1);
  }

  public long size() {
    return end - start + 1;
  }

  public Inet4Address startAddress() {
    return toAddress(start);
  }

  public Inet4Address endAddress() {
    return toAddress(end);
  }

  public List<Inet4Address> addresses() {
    return LongStream.rangeClosed(start, end).mapToObj(IpRange::toAddress).toList();
  }

  private static long toNumber(Inet4Address address) {
    return Integer.toUnsignedLong(ByteBuffer.wrap(address.getAddress()).getInt());
  }

  private static Inet4Address toAddress(long number) {
    try {
      return (Inet4Address)
          Inet4Address.getByAddress(ByteBuffer.allocate(4).putInt((int) number).array());
    } catch (UnknownHostException e) {
      throw new IllegalStateException("4 bytes are always a valid IPv4 address", e);
    }
  }
}
