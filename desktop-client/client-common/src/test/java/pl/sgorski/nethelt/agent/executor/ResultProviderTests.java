package pl.sgorski.nethelt.agent.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;
import pl.sgorski.nethelt.service.PingOperation;
import pl.sgorski.nethelt.service.TelnetOperation;

public class ResultProviderTests {

  private PingOperation ping;
  private TelnetOperation telnet;
  private ResultProvider resultProvider;

  @BeforeEach
  void setUp() {
    ping = mock(PingOperation.class);
    telnet = mock(TelnetOperation.class);
    resultProvider = new ResultProvider(ping, telnet);
  }

  @Test
  void getPingResults_ShouldReturnResults() {
    var device = new Device();
    var pingResult = mock(PingResult.class);

    when(ping.executeAsync(device)).thenReturn(CompletableFuture.completedFuture(pingResult));

    var results = resultProvider.getPingResults(Collections.singleton(device));

    assertEquals(1, results.size());
    assertEquals(pingResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldReturnResults_DevicesWithPort() {
    var deviceWithPort = new Device();
    deviceWithPort.setPort(22);
    var telnetResult = mock(TelnetResult.class);

    when(telnet.executeAsync(deviceWithPort))
      .thenReturn(CompletableFuture.completedFuture(telnetResult));

    var results = resultProvider.getTelnetResults(Collections.singleton(deviceWithPort));

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldFilterDevicesWithoutPort() {
    var deviceWithoutPort = new Device();
    var deviceWithPort = new Device();
    deviceWithPort.setPort(23);
    var devices = Set.of(deviceWithoutPort, deviceWithPort);

    var telnetResult = mock(TelnetResult.class);
    when(telnet.executeAsync(deviceWithPort)).thenReturn(CompletableFuture.completedFuture(telnetResult));

    var results = resultProvider.getTelnetResults(devices);

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }
}
