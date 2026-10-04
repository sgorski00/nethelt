package pl.sgorski.nethelt.agent.executor.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.ping.PingOperation;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;

@ExtendWith(MockitoExtension.class)
public class PingTaskHandlerTests {

  @Mock private PingOperation pingOperation;
  @Mock private MonitoringResultClient monitoringResultClient;
  @Mock private MonitoringResultMapper monitoringResultMapper;
  @Mock private MonitoringResultRequest request;
  @InjectMocks private PingTaskHandler pingTaskHandler;

  @Test
  void getOperation_shouldReturnPing() {
    assertEquals(TaskType.PING, pingTaskHandler.getOperation());
  }

  @Test
  void execute_shouldSendMappedResult() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var result = new PingResult(1L, true, "OK", 12L);
    when(pingOperation.execute(task)).thenReturn(result);
    when(monitoringResultMapper.toRequest(result)).thenReturn(request);

    pingTaskHandler.execute(task);

    verify(monitoringResultClient).sendMonitoringResult(request);
  }

  @Test
  void execute_shouldPropagateException_whenSendingFails() {
    var task = TestMonitoringTaskFactory.createPingMonitoringTask(1L);
    var result = new PingResult(1L, true, "OK", 12L);
    when(pingOperation.execute(task)).thenReturn(result);
    when(monitoringResultMapper.toRequest(result)).thenReturn(request);
    doThrow(new WebClientException("failed"))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    assertThrows(WebClientException.class, () -> pingTaskHandler.execute(task));
  }
}
