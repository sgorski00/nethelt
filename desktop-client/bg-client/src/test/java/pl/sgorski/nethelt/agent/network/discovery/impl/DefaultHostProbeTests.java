package pl.sgorski.nethelt.agent.network.discovery.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class DefaultHostProbeTests {

  private final Inet4Address address = mock(Inet4Address.class);
  private final DefaultHostProbe hostProbe = new DefaultHostProbe();

  @Test
  void isReachable_shouldPingWithTimeoutInMillis() throws Exception {
    when(address.isReachable(1500)).thenReturn(true);

    assertTrue(hostProbe.isReachable(address, Duration.ofMillis(1500)));
    verify(address).isReachable(1500);
  }

  @Test
  void resolveHostname_shouldReturnHostname_whenResolved() {
    when(address.getCanonicalHostName()).thenReturn("router.lan");
    when(address.getHostAddress()).thenReturn("192.168.0.1");

    assertEquals("router.lan", hostProbe.resolveHostname(address));
  }

  @Test
  void resolveHostname_shouldReturnNull_whenNotResolved() {
    when(address.getCanonicalHostName()).thenReturn("192.168.0.1");
    when(address.getHostAddress()).thenReturn("192.168.0.1");

    assertNull(hostProbe.resolveHostname(address));
  }
}
