package pl.sgorski.nethelt.agent.network.ping;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.network.ping.impl.DefaultPingOperation;
import pl.sgorski.nethelt.agent.test_utils.TestDeviceFactory;

public class DefaultPingOperationTests {

  private final Device device = mock(Device.class);
  private final InetAddress address = mock(InetAddress.class);
  private PingOperation pingOperation;

  @BeforeEach
  void setUp() {
    pingOperation = new DefaultPingOperation();
  }

  @Test
  void execute_SuccessfulPing() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getAddress()).thenReturn(address);
    when(address.isReachable(anyInt())).thenReturn(true);

    var result = pingOperation.execute(device);

    assertInstanceOf(PingResult.class, result);
    assertSame(device, result.getDevice());
    assertTrue(result.isSuccess());
    assertEquals("Ping successful", result.getMessage());
    assertTrue(result.getResponseTimeMs() >= 0);
  }

  @Test
  void execute_NotSuccessfulPing_NotReachable() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getAddress()).thenReturn(address);
    when(address.isReachable(anyInt())).thenReturn(false);

    var result = pingOperation.execute(device);

    assertInstanceOf(PingResult.class, result);
    assertSame(device, result.getDevice());
    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Timeout after"));
    assertTrue(result.getResponseTimeMs() >= 0);
  }

  @Test
  void execute_ShouldThrow_NetworkErrorOccurs() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getAddress()).thenReturn(address);
    when(address.isReachable(anyInt())).thenThrow(new IOException("A network error occurs!"));

    var ex = assertThrows(NetworkException.class, () -> pingOperation.execute(device));

    assertTrue(ex.getMessage().contains("Ping failed for device Device"));
  }

  @Test
  void error_shouldReturnErrorPingResult() {
    var device = TestDeviceFactory.createDeviceWithoutPort();

    var result = pingOperation.error(device);

    assertInstanceOf(PingResult.class, result);
    assertSame(device, result.getDevice());
    assertFalse(result.isSuccess());
    assertEquals("Ping failed", result.getMessage());
    assertEquals(-1, result.getResponseTimeMs());
  }
}
