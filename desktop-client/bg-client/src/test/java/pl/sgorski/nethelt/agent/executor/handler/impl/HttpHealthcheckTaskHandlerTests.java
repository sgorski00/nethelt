package pl.sgorski.nethelt.agent.executor.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.agent.model.monitoring_result.HttpHealthcheckResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.http_healthcheck.HttpHealthcheckOperation;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;

@ExtendWith(MockitoExtension.class)
public class HttpHealthcheckTaskHandlerTests {

  @Mock private HttpHealthcheckOperation httpHealthcheckOperation;
  @Mock private MonitoringResultClient monitoringResultClient;
  @Mock private MonitoringResultMapper monitoringResultMapper;
  @Mock private MonitoringResultRequest request;
  @InjectMocks private HttpHealthcheckTaskHandler httpHealthcheckTaskHandler;

  @Test
  void getOperation_shouldReturnHttpHealthcheck() {
    assertEquals(TaskType.HTTP_HEALTHCHECK, httpHealthcheckTaskHandler.getOperation());
  }

  @Test
  void execute_shouldSendMappedResult() {
    var task = createTask();
    var result = new HttpHealthcheckResult(1L, true, "OK", 12L, 200);
    when(httpHealthcheckOperation.execute(task)).thenReturn(result);
    when(monitoringResultMapper.toRequest(result)).thenReturn(request);

    httpHealthcheckTaskHandler.execute(task);

    verify(monitoringResultClient).sendMonitoringResult(request);
  }

  @Test
  void execute_shouldPropagateException_whenSendingFails() {
    var task = createTask();
    var result = new HttpHealthcheckResult(1L, true, "OK", 12L, 200);
    when(httpHealthcheckOperation.execute(task)).thenReturn(result);
    when(monitoringResultMapper.toRequest(result)).thenReturn(request);
    doThrow(new WebClientException("failed"))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    assertThrows(WebClientException.class, () -> httpHealthcheckTaskHandler.execute(task));
  }

  private static MonitoringTask createTask() {
    var config =
        TestMonitoringTaskFactory.createHttpHealthcheckTaskConfiguration(
            8080, "/health", null, Duration.ofSeconds(5));
    return TestMonitoringTaskFactory.createHttpHealthcheckMonitoringTask(
        1L, InetAddress.getLoopbackAddress(), config);
  }
}
