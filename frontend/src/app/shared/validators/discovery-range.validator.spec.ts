import { validateDiscoveryRange } from './discovery-range.validator';
import { ipRangeSize } from './ip-range-filter.validator';

describe('validateDiscoveryRange', () => {
  it('accepts a single address, subnet or range up to /24', () => {
    expect(validateDiscoveryRange('192.168.0.10')).toBeNull();
    expect(validateDiscoveryRange('192.168.0.0/24')).toBeNull();
    expect(validateDiscoveryRange('10.0.0.0-10.0.0.255')).toBeNull();
    expect(validateDiscoveryRange(' 192.168.0.0/28 ')).toBeNull();
  });

  it('rejects ranges larger than /24', () => {
    expect(validateDiscoveryRange('192.168.0.0/23')).not.toBeNull();
    expect(validateDiscoveryRange('10.0.0.0-10.0.1.0')).not.toBeNull();
  });

  it('rejects many values', () => {
    expect(validateDiscoveryRange('10.0.0.1, 10.0.0.2')).not.toBeNull();
  });

  it('rejects invalid values', () => {
    expect(validateDiscoveryRange('10.0.0.256')).not.toBeNull();
    expect(validateDiscoveryRange('10.0.0.0/33')).not.toBeNull();
  });
});

describe('ipRangeSize', () => {
  it('counts addresses in a value', () => {
    expect(ipRangeSize('10.0.0.5')).toBe(1);
    expect(ipRangeSize('10.0.0.0/24')).toBe(256);
    expect(ipRangeSize('10.0.0.0/0')).toBe(2 ** 32);
    expect(ipRangeSize('10.0.0.250 - 10.0.1.5')).toBe(12);
  });
});
