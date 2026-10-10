package pl.sgorski.nethelt.webapi.validator.range;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Duration;

public class RangeValidator implements ConstraintValidator<ValidRange, TimeRange> {

  private Duration maxLength = Duration.ZERO;

  @Override
  public void initialize(ValidRange annotation) {
    maxLength = Duration.ofDays(annotation.maxDays());
  }

  @Override
  public boolean isValid(TimeRange range, ConstraintValidatorContext context) {
    if (!range.from().isBefore(range.to())) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("'from' must be before 'to'")
          .addPropertyNode("from")
          .addConstraintViolation();
      return false;
    }

    if (Duration.between(range.from(), range.to()).compareTo(maxLength) > 0) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Range cannot be longer than {maxDays} days")
          .addPropertyNode("from")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
