import { FormControl } from '@angular/forms';
import { ipv4Validator } from './ip.validator';

describe('ipv4Validator', () => {
  const validate = (value: string | null) => ipv4Validator()(new FormControl(value));

  it('accepts valid IPv4 addresses', () => {
    expect(validate('192.168.0.1')).toBeNull();
    expect(validate('0.0.0.0')).toBeNull();
    expect(validate('255.255.255.255')).toBeNull();
  });

  it('accepts empty value', () => {
    expect(validate('')).toBeNull();
    expect(validate(null)).toBeNull();
  });

  it('rejects invalid addresses', () => {
    expect(validate('256.0.0.1')).toEqual({ ipv4: true });
    expect(validate('192.168.0')).toEqual({ ipv4: true });
    expect(validate('abc')).toEqual({ ipv4: true });
  });
});
