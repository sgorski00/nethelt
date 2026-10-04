package pl.sgorski.nethelt.agent.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.monitoring_result.HttpHealthcheckResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.PingResult;
import pl.sgorski.nethelt.agent.model.monitoring_result.TelnetResult;
import pl.sgorski.nethelt.agent.webclient.dto.request.HttpHealthcheckResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.PingResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.TelnetResultExtensionRequest;

public class MonitoringResultMapperTests {

  private MonitoringResultMapper monitoringResultMapper;

  @BeforeEach
  void setUp() {
    monitoringResultMapper = new MonitoringResultMapper();
  }

  @Test
  void toRequest_shouldMapPingResult() {
    var result = new PingResult(1L, true, "Ping successful", 12L);

    var request = monitoringResultMapper.toRequest(result);

    assertEquals(1L, request.taskId());
    assertEquals(result.getTimestamp(), request.executedAt());
    assertTrue(request.success());
    assertEquals("Ping successful", request.message());
    assertEquals(12L, request.responseTimeMs());
    assertInstanceOf(PingResultExtensionRequest.class, request.extension());
  }

  @Test
  void toRequest_shouldMapTelnetResult() {
    var result = new TelnetResult(2L, false, "Connection refused", null, false);

    var request = monitoringResultMapper.toRequest(result);

    assertEquals(2L, request.taskId());
    assertFalse(request.success());
    assertNull(request.responseTimeMs());
    var extension = assertInstanceOf(TelnetResultExtensionRequest.class, request.extension());
    assertFalse(extension.portOpen());
  }

  @Test
  void toRequest_shouldMapHttpHealthcheckResult() {
    var result = new HttpHealthcheckResult(3L, false, "Unexpected status code", 230L, 503);

    var request = monitoringResultMapper.toRequest(result);

    assertEquals(3L, request.taskId());
    assertEquals(230L, request.responseTimeMs());
    var extension =
        assertInstanceOf(HttpHealthcheckResultExtensionRequest.class, request.extension());
    assertEquals(503, extension.statusCode());
  }

  @Test
  void toRequest_shouldMapHttpHealthcheckResult_whenStatusCodeIsNull() {
    var result = new HttpHealthcheckResult(3L, false, "Connection refused", null, null);

    var request = monitoringResultMapper.toRequest(result);

    var extension =
        assertInstanceOf(HttpHealthcheckResultExtensionRequest.class, request.extension());
    assertNull(extension.statusCode());
  }
}
