package pl.sgorski.nethelt.agent.executor.handler.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.mapper.MonitoringResultMapper;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.network.telnet.TelnetOperation;
import pl.sgorski.nethelt.agent.service.MonitoringResultSender;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;

@ExtendWith(MockitoExtension.class)
public class TelnetTaskHandlerTests {

  @Mock private TelnetOperation telnetOperation;
  @Mock private MonitoringResultSender monitoringResultSender;
  @Mock private MonitoringResultMapper monitoringResultMapper;
  @Mock private MonitoringResultRequest request;
  @InjectMocks private TelnetTaskHandler telnetTaskHandler;

  @Test
  void getOperation_shouldReturnTelnet() {
    assertEquals(TaskType.TELNET, telnetTaskHandler.getOperation());
  }

  @Test
  void execute_shouldSendMappedResult() {
    var task = TestMonitoringTaskFactory.createTelnetMonitoringTask(1L);
    var result = new TelnetResult(1L, true, "OK", 12L, true);
    when(telnetOperation.execute(task)).thenReturn(result);
    when(monitoringResultMapper.toRequest(result)).thenReturn(request);

    telnetTaskHandler.execute(task);

    verify(monitoringResultSender).send(request);
  }
}
