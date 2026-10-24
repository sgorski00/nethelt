import { FormControl, FormGroup } from '@angular/forms';
import { passwordMatchValidator } from './password-match.validator';

describe('passwordMatchValidator', () => {
  const validate = (newPassword: string, repeatNewPassword: string) =>
    passwordMatchValidator(
      new FormGroup({
        newPassword: new FormControl(newPassword),
        repeatNewPassword: new FormControl(repeatNewPassword),
      }),
    );

  it('passes when passwords match', () => {
    expect(validate('secret', 'secret')).toBeNull();
  });

  it('fails when passwords differ', () => {
    expect(validate('secret', 'other')).toEqual({ passwordMismatch: true });
  });

  it('skips validation until both fields are filled', () => {
    expect(validate('secret', '')).toBeNull();
  });
});
