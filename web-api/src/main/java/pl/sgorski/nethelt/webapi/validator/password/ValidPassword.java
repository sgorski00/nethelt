package pl.sgorski.nethelt.webapi.validator.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {
  String message() default "Invalid password";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
