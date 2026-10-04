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
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.network.ping.impl.DefaultPingOperation;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;

public class DefaultPingOperationTests {

  private InetAddress address = mock(InetAddress.class);
  private PingOperation pingOperation;

  @BeforeEach
  void setUp() {
    pingOperation = new DefaultPingOperation();
  }

  @Test
  void execute_SuccessfulPing() throws Exception {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L, address);
    when(address.isReachable(anyInt())).thenReturn(true);

    var result = pingOperation.execute(task);

    assertInstanceOf(PingResult.class, result);
    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertEquals("Ping successful", result.getMessage());
    assertTrue(result.getResponseTimeMs() >= 0);
  }

  @Test
  void execute_NotSuccessfulPing_NotReachable() throws Exception {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L, address);
    when(address.isReachable(anyInt())).thenReturn(false);

    var result = pingOperation.execute(task);

    assertInstanceOf(PingResult.class, result);
    assertSame(1L, result.getTaskId());
    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Timeout after"));
    assertTrue(result.getResponseTimeMs() >= 0);
  }

  @Test
  void execute_ShouldThrow_NetworkErrorOccurs() throws Exception {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L, address);
    when(address.isReachable(anyInt())).thenThrow(new IOException("A network error occurs!"));

    var ex = assertThrows(NetworkException.class, () -> pingOperation.execute(task));

    assertTrue(ex.getMessage().contains("Ping failed for device 1"));
  }

  @Test
  void error_shouldReturnErrorPingResult() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L, address);

    var result = pingOperation.error(task);

    assertInstanceOf(PingResult.class, result);
    assertSame(1L, result.getTaskId());
    assertFalse(result.isSuccess());
    assertEquals("Ping failed", result.getMessage());
    assertNull(result.getResponseTimeMs());
  }
}
