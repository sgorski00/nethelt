package pl.sgorski.nethelt.agent.executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.executor.service.MonitoringExecutor;
import pl.sgorski.nethelt.agent.network.ping.PingOperation;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.agent.test_utils.TestResultFactory;

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
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var pingResult = TestResultFactory.createPingResult(true);
    when(ping.execute(task)).thenReturn(pingResult);

    var results = monitoringExecutor.getPingResults(Collections.singleton(task));

    assertEquals(1, results.size());
    assertSame(pingResult, results.iterator().next());
  }

  @Test
  void getPingResults_ShouldReturnErrorResult_WhenExecutionExceptionHappen() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var errorResult = TestResultFactory.createPingResult(false);
    when(ping.execute(task)).thenThrow(RuntimeException.class);
    when(ping.error(task)).thenReturn(errorResult);

    var results = monitoringExecutor.getPingResults(Collections.singleton(task));

    assertEquals(1, results.size());
    assertSame(errorResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldReturnResults() {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    var telnetResult = TestResultFactory.createTelnetResult(true);

    when(telnet.execute(task)).thenReturn(telnetResult);

    var results = monitoringExecutor.getTelnetResults(Collections.singleton(task));

    assertEquals(1, results.size());
    assertEquals(telnetResult, results.iterator().next());
  }

  @Test
  void getTelnetResults_ShouldReturnErrorResult_WhenExecutionExceptionHappen() {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    var errorResult = TestResultFactory.createTelnetResult(false);
    when(telnet.execute(task)).thenThrow(RuntimeException.class);
    when(telnet.error(task)).thenReturn(errorResult);

    var results = monitoringExecutor.getTelnetResults(Collections.singleton(task));

    assertEquals(1, results.size());
    assertEquals(errorResult, results.iterator().next());
  }
}
