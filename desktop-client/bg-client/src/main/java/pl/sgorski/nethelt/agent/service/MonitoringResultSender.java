package pl.sgorski.nethelt.agent.service;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import pl.sgorski.nethelt.agent.buffer.ResultBuffer;
import pl.sgorski.nethelt.agent.buffer.ResultBufferProperties;
import pl.sgorski.nethelt.agent.exception.CredentialsStoreException;
import pl.sgorski.nethelt.agent.exception.MissingCredentialsException;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringResultSender {

  private final MonitoringResultClient monitoringResultClient;
  private final ResultBuffer resultBuffer;
  private final ResultBufferProperties properties;

  public void send(MonitoringResultRequest request) {
    try {
      monitoringResultClient.sendMonitoringResult(request);
    } catch (Exception e) {
      if (isTemporary(e)) {
        resultBuffer.add(request);
        log.warn("Result of task {} was buffered: {}", request.taskId(), e.getMessage());
        return;
      }
      log.warn("Result of task {} was dropped: {}", request.taskId(), e.getMessage());
    }
  }

  /**
   * Sends buffered results oldest first and removes them only after the Web API accepted or
   * rejected them. Stops at the first temporary error, the rest is sent in the next flush.
   */
  public void flushBuffer() {
    var batch = resultBuffer.peek(properties.flushBatchSize());
    if (batch.isEmpty()) return;

    var processed = new ArrayList<MonitoringResultRequest>(batch.size());
    var sent = 0;
    for (var request : batch) {
      try {
        monitoringResultClient.sendMonitoringResult(request);
        sent++;
      } catch (Exception e) {
        if (isTemporary(e)) {
          log.warn("Flush of result buffer stopped: {}", e.getMessage());
          break;
        }
        log.warn("Buffered result of task {} was dropped: {}", request.taskId(), e.getMessage());
      }
      processed.add(request);
    }
    resultBuffer.remove(processed);

    if (sent > 0) {
      log.info("Sent {} buffered result(s), {} left", sent, resultBuffer.size());
    }
  }

  static boolean isTemporary(Exception e) {
    return switch (e) {
      case ResourceAccessException _, MissingCredentialsException _, CredentialsStoreException _ ->
          true;
      case WebClientException webClientException -> isTemporary(webClientException.getStatus());
      default -> false;
    };
  }

  private static boolean isTemporary(HttpStatusCode status) {
    return status.is5xxServerError()
        || status.isSameCodeAs(HttpStatus.UNAUTHORIZED)
        || status.isSameCodeAs(HttpStatus.REQUEST_TIMEOUT)
        || status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS);
  }
}
