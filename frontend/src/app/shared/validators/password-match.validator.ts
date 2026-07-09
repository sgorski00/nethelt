import { AbstractControl, ValidationErrors } from '@angular/forms';

const NEW_PASSWORD_FIELD = 'newPassword';
const CONFIRM_PASSWORD_FIELD = 'repeatNewPassword';

export function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get(NEW_PASSWORD_FIELD)?.value;
  const repeatPassword = control.get(CONFIRM_PASSWORD_FIELD)?.value;

  if (!password || !repeatPassword) {
    return null;
  }

  return password === repeatPassword ? null : { passwordMismatch: true };
}
