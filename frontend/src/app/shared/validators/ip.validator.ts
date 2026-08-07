import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const IPV4_REGEX = /^(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$/;

export function ipv4Validator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value?.trim();

    if (!value) return null;

    return IPV4_REGEX.test(value) ? null : { ipv4: true };
  };
}
