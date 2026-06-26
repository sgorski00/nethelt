package pl.sgorski.nethelt.webapi.validator.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.sgorski.nethelt.webapi.features.user.dto.contract.PasswordChange;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, PasswordChange> {

    /**
     * Regex explanation:
     * <ul>
     * <li>^: Start of string</li>
     * <li>(?=.*[A-Z]): At least one uppercase letter</li>
     * <li>(?=.*[a-z]): At least one lowercase letter</li>
     * <li>(?=.*\d): At least one digit</li>
     * <li>(?=.*[^a-zA-Z0-9]): At least one special character (non-alphanumeric)</li></li>
     * <li>(?=\S+$): No whitespace allowed</li>
     * <li>.{8,}: At least 8 characters long</li>
     * <li>$: End of string</li>
     * </ul>
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^a-zA-Z0-9])(?=\\S+$).{8,}$");

    @Override
    public boolean isValid(PasswordChange request, ConstraintValidatorContext context) {

        if (!request.newPassword().equals(request.repeatNewPassword())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode("repeatNewPassword")
                    .addConstraintViolation();
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Password must be at least 8 chars, contain uppercase letter, digit and special char"
                    ).addPropertyNode("newPassword")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
