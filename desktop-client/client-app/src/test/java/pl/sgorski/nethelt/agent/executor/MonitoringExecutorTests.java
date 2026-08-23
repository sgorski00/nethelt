package pl.sgorski.nethelt.agent.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.model.TelnetResult;
import pl.sgorski.nethelt.agent.network.ping.PingOperation;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;
import pl.sgorski.nethelt.agent.test_utils.TestDeviceFactory;

@ExtendWith(MockitoExtension.class)
public class MonitoringExecutorTests {

  @Mock private PingOperation ping;
  @Mock private TelnetOperation telnet;
  @InjectMocks private MonitoringExecutor monitoringExecutor;

  @BeforeEach
  void setUp() {
    monitoringExecutor =
        new MonitoringExecutor(ping, telnet, Executors.newVirtualThreadPerTaskExecutor());
  }

  @Test
  void getPingResults_ShouldReturnResults() {
    var device = TestDeviceFactory.createDeviceWithoutPort();
    var pingResult = mock(PingResult.class);

    when(ping.execute(device)).thenReturn(pingResult);

    var results = monitoringExecutor.getPingResults(Collections.singleton(device));

    assertEquals(1, results.size());
    assertEquals(pingResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldReturnResults_DevicesWithPort() {
    var deviceWithPort = TestDeviceFactory.createDeviceWithPort(22);
    var telnetResult = mock(TelnetResult.class);

    when(telnet.execute(deviceWithPort)).thenReturn(telnetResult);

    var results = monitoringExecutor.getTelnetResults(Collections.singleton(deviceWithPort));

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldFilterDevicesWithoutPort() {
    var deviceWithoutPort = TestDeviceFactory.createDeviceWithoutPort();
    var deviceWithPort = TestDeviceFactory.createDeviceWithPort(23);
    var devices = Set.of(deviceWithoutPort, deviceWithPort);

    var telnetResult = mock(TelnetResult.class);
    when(telnet.execute(deviceWithPort)).thenReturn(telnetResult);

    var results = monitoringExecutor.getTelnetResults(devices);

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }
}
