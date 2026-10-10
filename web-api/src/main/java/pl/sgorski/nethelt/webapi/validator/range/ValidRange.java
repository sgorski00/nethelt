package pl.sgorski.nethelt.webapi.validator.range;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Checks that {@code from} is before {@code to} and the range is at most {@link #maxDays()}. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RangeValidator.class)
@Documented
public @interface ValidRange {
  String message() default "Invalid range";

  long maxDays();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
