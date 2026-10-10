package pl.sgorski.nethelt.webapi.features.metrics.dto.param;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MetricsRangeTests {

  private static final Instant TO = Instant.parse("2026-10-10T11:00:00Z");

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void constructor_shouldDefaultToLastDay_whenNoBoundsGiven() {
    var before = Instant.now();

    var range = new MetricsRange(null, null);

    assertFalse(range.to().isBefore(before));
    assertEquals(MetricsRange.DEFAULT_LENGTH, range.length());
  }

  @Test
  void constructor_shouldDefaultFromToDayBeforeTo_whenOnlyToGiven() {
    var range = new MetricsRange(null, TO);

    assertEquals(TO.minus(MetricsRange.DEFAULT_LENGTH), range.from());
  }

  @Test
  void validate_shouldPass_whenRangeAtMaxLength() {
    var range = new MetricsRange(TO.minus(Duration.ofDays(31)), TO);

    assertTrue(validator.validate(range).isEmpty());
  }

  @Test
  void validate_shouldFail_whenRangeLongerThanMonth() {
    var range = new MetricsRange(TO.minus(Duration.ofDays(31)).minusSeconds(1), TO);

    var violations = validator.validate(range);

    assertEquals(1, violations.size());
    assertEquals("Range cannot be longer than 31 days", violations.iterator().next().getMessage());
  }
}
