package pl.sgorski.nethelt.agent.network.discovery.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.network.discovery.HostProbe;

@Component
public final class DefaultHostProbe implements HostProbe {

  @Override
  public boolean isReachable(Inet4Address address, Duration timeout) throws IOException {
    return address.isReachable((int) timeout.toMillis());
  }

  @Override
  public @Nullable String resolveHostname(Inet4Address address) {
    var hostname = address.getCanonicalHostName();
    return hostname.equals(address.getHostAddress()) ? null : hostname;
  }
}
