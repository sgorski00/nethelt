package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.PingResultExtensionRequest;

public class MonitoringResultBatchAddRequestTests {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void validate_shouldPass_whenBatchIsValid() {
    var request = new MonitoringResultBatchAddRequest(List.of(result(1L), result(2L)));

    assertTrue(validator.validate(request).isEmpty());
  }

  @Test
  void validate_shouldPass_whenBatchHasMaxSize() {
    var request =
        new MonitoringResultBatchAddRequest(results(MonitoringResultBatchAddRequest.MAX_SIZE));

    assertTrue(validator.validate(request).isEmpty());
  }

  @Test
  void validate_shouldFail_whenBatchIsEmpty() {
    var request = new MonitoringResultBatchAddRequest(List.of());

    assertEquals(1, validator.validate(request).size());
  }

  @Test
  void validate_shouldFail_whenBatchIsTooLarge() {
    var request =
        new MonitoringResultBatchAddRequest(results(MonitoringResultBatchAddRequest.MAX_SIZE + 1));

    assertEquals(1, validator.validate(request).size());
  }

  @Test
  void validate_shouldFail_whenResultIsInvalid() {
    var invalid =
        new MonitoringResultAddRequest(
            1L, Instant.now(), true, null, -1L, new PingResultExtensionRequest());
    var request = new MonitoringResultBatchAddRequest(List.of(result(1L), invalid));

    assertEquals(1, validator.validate(request).size());
  }

  @Test
  void validate_shouldFail_whenResultIsNull() {
    var results = new ArrayList<>(Collections.singletonList((MonitoringResultAddRequest) null));
    var request = new MonitoringResultBatchAddRequest(results);

    assertEquals(1, validator.validate(request).size());
  }

  private static List<MonitoringResultAddRequest> results(int count) {
    return IntStream.range(0, count).mapToObj(i -> result((long) i + 1)).toList();
  }

  private static MonitoringResultAddRequest result(Long taskId) {
    return new MonitoringResultAddRequest(
        taskId, Instant.now(), true, "OK", 12L, new PingResultExtensionRequest());
  }
}
