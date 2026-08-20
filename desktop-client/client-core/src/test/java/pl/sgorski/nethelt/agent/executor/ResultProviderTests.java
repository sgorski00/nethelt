package pl.sgorski.nethelt.agent.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.service.PingOperation;
import pl.sgorski.nethelt.agent.service.TelnetOperation;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;

@ExtendWith(MockitoExtension.class)
public class ResultProviderTests {

  @Mock private PingOperation ping;
  @Mock private TelnetOperation telnet;
  @InjectMocks private MonitoringExecutor MonitoringExecutor;

  @Test
  void getPingResults_ShouldReturnResults() {
    var device = new Device();
    var pingResult = mock(PingResult.class);

    when(ping.executeAsync(device)).thenReturn(CompletableFuture.completedFuture(pingResult));

    var results = MonitoringExecutor.getPingResults(Collections.singleton(device));

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

    var results = MonitoringExecutor.getTelnetResults(Collections.singleton(deviceWithPort));

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
    when(telnet.executeAsync(deviceWithPort))
        .thenReturn(CompletableFuture.completedFuture(telnetResult));

    var results = MonitoringExecutor.getTelnetResults(devices);

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }
}
