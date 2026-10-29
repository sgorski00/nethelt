package pl.sgorski.nethelt.agent.network.discovery;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.exception.NetworkException;

public class LocalSubnetResolverTests {

  @Test
  void resolve_shouldReturnAtMost24Subnet_whenMachineHasNetworkInterface() {
    try {
      var range = new LocalSubnetResolver().resolve();

      assertTrue(range.size() >= 1 && range.size() <= 254, "size: " + range.size());
      assertFalse(range.startAddress().isLoopbackAddress());
    } catch (NetworkException e) {
      assertEquals("No IPv4 network interface found", e.getMessage());
    }
  }
}
