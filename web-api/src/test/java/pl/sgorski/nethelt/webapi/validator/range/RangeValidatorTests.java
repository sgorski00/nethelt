package pl.sgorski.nethelt.webapi.validator.range;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintValidatorContext;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RangeValidatorTests {

  private static final Instant TO = Instant.parse("2026-10-10T11:00:00Z");

  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @Mock
  private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
      nodeBuilder;

  @Mock private ValidRange annotation;

  private RangeValidator validator;

  @BeforeEach
  void setUp() {
    when(annotation.maxDays()).thenReturn(31L);
    validator = new RangeValidator();
    validator.initialize(annotation);
  }

  @Test
  void isValid_shouldReturnTrue_whenRangeAtMaxLength() {
    var range = new TestTimeRange(TO.minus(Duration.ofDays(31)), TO);

    var result = validator.isValid(range, context);

    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void isValid_shouldReturnFalse_whenFromEqualsTo() {
    mockProperties();

    var result = validator.isValid(new TestTimeRange(TO, TO), context);

    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate("'from' must be before 'to'");
    verify(violationBuilder).addPropertyNode("from");
    verify(nodeBuilder).addConstraintViolation();
  }

  @Test
  void isValid_shouldReturnFalse_whenFromAfterTo() {
    mockProperties();

    var result = validator.isValid(new TestTimeRange(TO.plusSeconds(1), TO), context);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenRangeTooLong() {
    var range = new TestTimeRange(TO.minus(Duration.ofDays(31)).minusSeconds(1), TO);
    mockProperties();

    var result = validator.isValid(range, context);

    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context)
        .buildConstraintViolationWithTemplate("Range cannot be longer than {maxDays} days");
    verify(violationBuilder).addPropertyNode("from");
    verify(nodeBuilder).addConstraintViolation();
  }

  private void mockProperties() {
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
    when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
  }

  private record TestTimeRange(Instant from, Instant to) implements TimeRange {}
}
