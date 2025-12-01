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
    Device device = new Device();
    PingResult pingResult = mock(PingResult.class);

    when(ping.executeAsync(device)).thenReturn(CompletableFuture.completedFuture(pingResult));

    Set<PingResult> results = resultProvider.getPingResults(Collections.singleton(device));

    assertEquals(1, results.size());
    assertEquals(pingResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldReturnResults_DevicesWithPort() {
    Device deviceWithPort = new Device();
    deviceWithPort.setPort(22);
    TelnetResult telnetResult = mock(TelnetResult.class);

    when(telnet.executeAsync(deviceWithPort))
      .thenReturn(CompletableFuture.completedFuture(telnetResult));

    Set<TelnetResult> results = resultProvider.getTelnetResults(Collections.singleton(deviceWithPort));

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldFilterDevicesWithoutPort() {
    Device deviceWithoutPort = new Device();
    Device deviceWithPort = new Device();
    deviceWithPort.setPort(23);
    Set<Device> devices = Set.of(deviceWithoutPort, deviceWithPort);

    TelnetResult telnetResult = mock(TelnetResult.class);
    when(telnet.executeAsync(deviceWithPort)).thenReturn(CompletableFuture.completedFuture(telnetResult));

    Set<TelnetResult> results = resultProvider.getTelnetResults(devices);

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }
}
