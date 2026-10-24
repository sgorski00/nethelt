import { MAX_IP_RANGES, validateIpRangeFilter } from './ip-range-filter.validator';

describe('validateIpRangeFilter', () => {
  it('accepts single addresses, ranges and subnets', () => {
    expect(validateIpRangeFilter('10.0.0.5')).toBeNull();
    expect(validateIpRangeFilter('10.0.0.1-10.0.0.50')).toBeNull();
    expect(validateIpRangeFilter('192.168.0.0/24')).toBeNull();
    expect(validateIpRangeFilter('192.168.0.0/24, 10.0.0.1-10.0.0.50, 10.0.0.5')).toBeNull();
  });

  it('rejects empty values', () => {
    expect(validateIpRangeFilter('10.0.0.1,,10.0.0.2')).not.toBeNull();
  });

  it('rejects too many values', () => {
    const filter = Array.from({ length: MAX_IP_RANGES + 1 }, (_, i) => `10.0.0.${i}`).join(',');
    expect(validateIpRangeFilter(filter)).not.toBeNull();
  });

  it('rejects invalid addresses', () => {
    expect(validateIpRangeFilter('10.0.0.256')).not.toBeNull();
  });

  it('rejects range with start greater than end', () => {
    expect(validateIpRangeFilter('10.0.0.50-10.0.0.1')).not.toBeNull();
  });

  it('rejects invalid subnet mask', () => {
    expect(validateIpRangeFilter('10.0.0.0/33')).not.toBeNull();
    expect(validateIpRangeFilter('10.0.0.0/abc')).not.toBeNull();
  });
});
