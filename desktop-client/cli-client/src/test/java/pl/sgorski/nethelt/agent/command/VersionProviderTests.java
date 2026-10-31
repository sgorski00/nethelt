package pl.sgorski.nethelt.agent.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class VersionProviderTests {

  @Test
  void getVersion_shouldReturnCliVersion() {
    var provider = new VersionProvider("1.2.3");

    assertArrayEquals(new String[] {"Nethelt CLI 1.2.3"}, provider.getVersion());
  }
}
