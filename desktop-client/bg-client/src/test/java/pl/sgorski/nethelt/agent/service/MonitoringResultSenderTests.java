package pl.sgorski.nethelt.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import pl.sgorski.nethelt.agent.buffer.ResultBuffer;
import pl.sgorski.nethelt.agent.buffer.ResultBufferProperties;
import pl.sgorski.nethelt.agent.exception.CredentialsAccessDeniedException;
import pl.sgorski.nethelt.agent.exception.MissingCredentialsException;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultBatchRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.PingResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.MonitoringResultBatchResponse;

@ExtendWith(MockitoExtension.class)
public class MonitoringResultSenderTests {

  private static final int BATCH_SIZE = 3;

  @Mock private MonitoringResultClient monitoringResultClient;
  @Mock private ResultBuffer resultBuffer;
  private MonitoringResultSender sender;

  @BeforeEach
  void setUp() {
    sender =
        new MonitoringResultSender(
            monitoringResultClient, resultBuffer, new ResultBufferProperties("", 10, BATCH_SIZE));
  }

  @Test
  void send_shouldSendResult_andNotBufferIt() {
    var request = createRequest(1L);

    sender.send(request);

    verify(monitoringResultClient).sendMonitoringResult(request);
    verifyNoInteractions(resultBuffer);
  }

  @Test
  void send_shouldBufferResult_whenApiIsUnreachable() {
    var request = createRequest(1L);
    doThrow(new ResourceAccessException("Connection refused"))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    assertDoesNotThrow(() -> sender.send(request));

    verify(resultBuffer).add(request);
  }

  @Test
  void send_shouldBufferResult_whenCredentialsAreMissing() {
    var request = createRequest(1L);
    doThrow(new MissingCredentialsException("no PAT"))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    sender.send(request);

    verify(resultBuffer).add(request);
  }

  @Test
  void send_shouldBufferResult_whenCredentialsAccessIsDenied() {
    var request = createRequest(1L);
    var file = Path.of("/etc/nethelt/credentials");
    doThrow(new CredentialsAccessDeniedException(file, new AccessDeniedException(file.toString())))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    sender.send(request);

    verify(resultBuffer).add(request);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {
        "INTERNAL_SERVER_ERROR",
        "BAD_GATEWAY",
        "SERVICE_UNAVAILABLE",
        "UNAUTHORIZED",
        "REQUEST_TIMEOUT",
        "TOO_MANY_REQUESTS"
      })
  void send_shouldBufferResult_whenApiFailsTemporarily(HttpStatus status) {
    var request = createRequest(1L);
    doThrow(new WebClientException(status))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    sender.send(request);

    verify(resultBuffer).add(request);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"BAD_REQUEST", "FORBIDDEN", "NOT_FOUND", "CONFLICT", "UNPROCESSABLE_CONTENT"})
  void send_shouldDropResult_whenApiRejectsIt(HttpStatus status) {
    var request = createRequest(1L);
    doThrow(new WebClientException(status))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    assertDoesNotThrow(() -> sender.send(request));

    verifyNoInteractions(resultBuffer);
  }

  @Test
  void send_shouldDropResult_whenRequestIsInvalid() {
    var request = createRequest(1L);
    doThrow(new ConstraintViolationException(Set.of()))
        .when(monitoringResultClient)
        .sendMonitoringResult(request);

    sender.send(request);

    verifyNoInteractions(resultBuffer);
  }

  @Test
  void flushBuffer_shouldDoNothing_whenBufferIsEmpty() {
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of());

    sender.flushBuffer();

    verifyNoInteractions(monitoringResultClient);
    verify(resultBuffer, never()).remove(any());
  }

  @Test
  void flushBuffer_shouldSendResultsInOneBatch_andRemoveThem() {
    var first = createRequest(1L);
    var second = createRequest(2L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first, second));
    when(monitoringResultClient.sendMonitoringResults(
            new MonitoringResultBatchRequest(List.of(first, second))))
        .thenReturn(new MonitoringResultBatchResponse(1, 1, 0));

    sender.flushBuffer();

    verify(resultBuffer).remove(List.of(first, second));
    verify(monitoringResultClient, never()).sendMonitoringResult(any());
  }

  @Test
  void flushBuffer_shouldKeepResults_whenBatchFailsTemporarily() {
    var first = createRequest(1L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first));
    when(monitoringResultClient.sendMonitoringResults(any()))
        .thenThrow(new ResourceAccessException("Connection refused"));

    sender.flushBuffer();

    verify(resultBuffer, never()).remove(any());
    verify(monitoringResultClient, never()).sendMonitoringResult(any());
  }

  @Test
  void flushBuffer_shouldSendOneByOneInOrder_whenBatchIsRejected() {
    var first = createRequest(1L);
    var second = createRequest(2L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first, second));
    rejectBatchEndpoint();

    sender.flushBuffer();

    var inOrder = inOrder(monitoringResultClient, resultBuffer);
    inOrder.verify(monitoringResultClient).sendMonitoringResult(first);
    inOrder.verify(monitoringResultClient).sendMonitoringResult(second);
    inOrder.verify(resultBuffer).remove(List.of(first, second));
  }

  @Test
  void flushBuffer_shouldSendOneByOne_whenBatchIsInvalid() {
    var first = createRequest(1L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first));
    when(monitoringResultClient.sendMonitoringResults(any()))
        .thenThrow(new ConstraintViolationException(Set.of()));

    sender.flushBuffer();

    verify(monitoringResultClient).sendMonitoringResult(first);
    verify(resultBuffer).remove(List.of(first));
  }

  @Test
  void flushBuffer_shouldStopOneByOne_andKeepRemainingResults_whenApiFailsTemporarily() {
    var first = createRequest(1L);
    var second = createRequest(2L);
    var third = createRequest(3L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first, second, third));
    rejectBatchEndpoint();
    doThrow(new ResourceAccessException("Connection refused"))
        .when(monitoringResultClient)
        .sendMonitoringResult(second);

    sender.flushBuffer();

    verify(monitoringResultClient, never()).sendMonitoringResult(third);
    verify(resultBuffer).remove(List.of(first));
    verify(resultBuffer, never()).add(any());
  }

  @Test
  void flushBuffer_shouldRemoveRejectedResult_andContinueOneByOne() {
    var first = createRequest(1L);
    var second = createRequest(2L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first, second));
    rejectBatchEndpoint();
    doThrow(new WebClientException(HttpStatus.NOT_FOUND))
        .when(monitoringResultClient)
        .sendMonitoringResult(first);

    sender.flushBuffer();

    verify(monitoringResultClient).sendMonitoringResult(second);
    verify(resultBuffer).remove(List.of(first, second));
  }

  @Test
  void flushBuffer_shouldKeepAllResults_whenFirstResultFailsTemporarilyOneByOne() {
    var first = createRequest(1L);
    when(resultBuffer.peek(BATCH_SIZE)).thenReturn(List.of(first));
    rejectBatchEndpoint();
    doThrow(new WebClientException(HttpStatus.SERVICE_UNAVAILABLE))
        .when(monitoringResultClient)
        .sendMonitoringResult(first);

    sender.flushBuffer();

    verify(resultBuffer).remove(List.of());
  }

  private void rejectBatchEndpoint() {
    when(monitoringResultClient.sendMonitoringResults(any()))
        .thenThrow(new WebClientException(HttpStatus.NOT_FOUND));
  }

  private static MonitoringResultRequest createRequest(Long taskId) {
    return new MonitoringResultRequest(
        taskId,
        Instant.parse("2026-11-04T12:00:00Z"),
        true,
        "OK",
        12L,
        new PingResultExtensionRequest());
  }
}
