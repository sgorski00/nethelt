package pl.sgorski.nethelt.webapi.validator.password;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.user.dto.contract.PasswordChange;

@ExtendWith(MockitoExtension.class)
public class PasswordValidatorTests {
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @Mock
  private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
      nodeBuilder;

  private PasswordValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PasswordValidator();
  }

  @Test
  void isValid_shouldReturnTrue_whenPasswordMeetsRequirements() {
    var request = new TestPasswordChange("Password1!", "Password1!");

    var result = validator.isValid(request, context);

    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordsDoNotMatch() {
    var request = new TestPasswordChange("Password1!", "Different1!");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate("Passwords do not match");
    verify(violationBuilder).addPropertyNode("repeatNewPassword");
    verify(nodeBuilder).addConstraintViolation();
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordDoesNotMeetRequirements() {
    var request = new TestPasswordChange("password", "password");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context)
        .buildConstraintViolationWithTemplate(
            "Password must be at least 8 chars, contain uppercase letter, digit and special char");
    verify(violationBuilder).addPropertyNode("newPassword");
    verify(nodeBuilder).addConstraintViolation();
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordHasNoUppercaseLetter() {
    var request = new TestPasswordChange("password1!", "password1!");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordHasNoDigit() {
    var request = new TestPasswordChange("Password!", "Password!");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordHasNoSpecialCharacter() {
    var request = new TestPasswordChange("Password1", "Password1");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordContainsWhitespace() {
    var request = new TestPasswordChange("Password 1!", "Password 1!");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenPasswordIsTooShort() {
    var request = new TestPasswordChange("Pass1!", "Pass1!");
    mockProperties();

    var result = validator.isValid(request, context);

    assertFalse(result);
  }

  private void mockProperties() {
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
    when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
  }

  private record TestPasswordChange(String newPassword, String repeatNewPassword)
      implements PasswordChange {}
}
