package pl.sgorski.nethelt.agent.network.telnet;

import static org.junit.jupiter.api.Assertions.*;
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
import pl.sgorski.nethelt.agent.network.telnet.impl.DefaultTelnetOperation;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;

@ExtendWith(MockitoExtension.class)
public class DefaultTelnetOperationTests {

  @Mock private Socket socket;
  @Mock private SocketFactory socketFactory;
  @InjectMocks private DefaultTelnetOperation telnetOperation;

  @Test
  void execute_SuccessfulTelnet() throws Exception {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    when(socketFactory.createSocket()).thenReturn(socket);

    var result = telnetOperation.execute(task);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertTrue(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is open in device 1", result.getMessage());
  }

  @Test
  void execute_FailureTelnet_PortClosedConnectException() throws Exception {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(ConnectException.class).when(socket).connect(any(), anyInt());

    var result = telnetOperation.execute(task);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is closed in device 1", result.getMessage());
  }

  @Test
  void execute_FailureTelnet_PortClosedSocketTimeoutException() throws Exception {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(SocketTimeoutException.class).when(socket).connect(any(), anyInt());

    var result = telnetOperation.execute(task);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is closed in device 1", result.getMessage());
  }

  @Test
  void execute_FailureTelnet_PortClosedIllegalBlockingModeException() throws Exception {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(IllegalBlockingModeException.class).when(socket).connect(any(), anyInt());

    var result = telnetOperation.execute(task);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertTrue(result.getResponseTimeMs() >= 0);
    assertEquals("Port 80 is closed in device 1", result.getMessage());
  }

  @Test
  void execute_shouldThrowNetworkException_IOException() throws Exception {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(IOException.class).when(socket).connect(any(), anyInt());

    assertThrows(NetworkException.class, () -> telnetOperation.execute(task));
  }

  @Test
  void execute_shouldThrowNetworkException_IllegalArgumentException() throws Exception {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    when(socketFactory.createSocket()).thenReturn(socket);
    doThrow(IllegalArgumentException.class).when(socket).connect(any(), anyInt());

    assertThrows(NetworkException.class, () -> telnetOperation.execute(task));
  }

  @Test
  void error_shouldReturnErrorTelnetResult() {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);

    var result = telnetOperation.error(task);

    assertSame(task.id(), result.getTaskId());
    assertFalse(result.isSuccess());
    assertFalse(result.isPortOpen());
    assertEquals("Telnet check failed", result.getMessage());
    assertEquals(-1, result.getResponseTimeMs());
  }
}
