package pl.sgorski.nethelt.network.ping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.exception.NetworkException;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.service.PingOperation;

public class DefaultPingOperationImplTests {

  private final Device device = mock(Device.class);
  private final InetAddress address = mock(InetAddress.class);
  private final PingOperation pingOperation = new DefaultPingOperationImpl();

  @Test
  void execute_SuccessfulPing() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getAddress()).thenReturn(address);
    when(address.isReachable(anyInt())).thenReturn(true);

    var result = pingOperation.execute(device);

    assertInstanceOf(PingResult.class, result);
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
  void executeAsync_SuccessfulPing_DeviceIsReachable() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getAddress()).thenReturn(address);
    when(address.isReachable(anyInt())).thenReturn(true);

    var futureResult = pingOperation.executeAsync(device);
    var result = futureResult.get();

    assertTrue(result.isSuccess());
    assertEquals("Ping successful", result.getMessage());
    assertEquals(device, result.getDevice());
  }

  @Test
  void executeAsync_ThrowsNetworkException_IOExceptionOccurs() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getAddress()).thenReturn(address);
    when(address.isReachable(anyInt())).thenThrow(new IOException("Connection error"));

    var futureResult = pingOperation.executeAsync(device);

    var exception = assertThrows(ExecutionException.class, futureResult::get);
    assertInstanceOf(NetworkException.class, exception.getCause());
  }
}
