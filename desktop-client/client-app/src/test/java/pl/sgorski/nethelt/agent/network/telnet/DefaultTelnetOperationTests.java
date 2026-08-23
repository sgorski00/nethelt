package pl.sgorski.nethelt.agent.network.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import javax.net.SocketFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.Device;
import pl.sgorski.nethelt.agent.network.telnet.impl.DefaultTelnetOperation;

@ExtendWith(MockitoExtension.class)
public class DefaultTelnetOperationTests {

  @Mock private Device device;
  @Mock private Socket socket;
  @Mock private SocketFactory socketFactory;
  @InjectMocks private DefaultTelnetOperation telnetOperation;

  @Test
  void execute_SuccessfulTelnet() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(80);
    when(socketFactory.createSocket()).thenReturn(socket);

    var result = telnetOperation.execute(device);

    assertTrue(result.isSuccess());
    assertTrue(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is open in device Device", result.getMessage());
  }

  @Test
  void execute_FailureTelnet_PortClosedConnectException() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(80);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(ConnectException.class).when(socket).connect(any(), anyInt());

    var result = telnetOperation.execute(device);

    assertTrue(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is closed in device Device", result.getMessage());
  }

  @Test
  void execute_FailureTelnet_PortClosedSocketTimeoutException() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(80);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(SocketTimeoutException.class).when(socket).connect(any(), anyInt());

    var result = telnetOperation.execute(device);

    assertTrue(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is closed in device Device", result.getMessage());
  }

  @Test
  void execute_FailureTelnet_PortClosedIllegalBlockingModeException() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(80);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(IllegalBlockingModeException.class).when(socket).connect(any(), anyInt());

    var result = telnetOperation.execute(device);

    assertTrue(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is closed in device Device", result.getMessage());
  }

  @Test
  void execute_shouldThrowNetworkException_IOException() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(80);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(IOException.class).when(socket).connect(any(), anyInt());

    assertThrows(NetworkException.class, () -> telnetOperation.execute(device));
  }

  @Test
  void execute_shouldThrowNetworkException_IllegalArgumentException() throws Exception {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(80);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(IllegalArgumentException.class).when(socket).connect(any(), anyInt());

    assertThrows(NetworkException.class, () -> telnetOperation.execute(device));
  }

  @Test
  void execute_shouldThrowIllegalArgumentException_PortIsNull() {
    when(device.getName()).thenReturn("Device");
    when(device.getPort()).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> telnetOperation.execute(device));
  }
}
