package pl.sgorski.nethelt.agent.network.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.time.Duration;
import org.jspecify.annotations.Nullable;

public interface HostProbe {
  boolean isReachable(Inet4Address address, Duration timeout) throws IOException;

  @Nullable String resolveHostname(Inet4Address address);
}
