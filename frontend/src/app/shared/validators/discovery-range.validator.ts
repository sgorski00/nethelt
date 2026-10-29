import { ipRangeSize, validateIpRangeFilter } from './ip-range-filter.validator';

export const MAX_DISCOVERY_RANGE_SIZE = 256;

/**
 * Validates the range of a discovery scan: a single address, subnet or range of at most 256
 * addresses. Returns an error message, or null when the range is valid.
 */
export function validateDiscoveryRange(value: string): string | null {
  if (value.includes(',')) return 'Enter a single address, subnet or range.';

  const error = validateIpRangeFilter(value);
  if (error) return error;

  return ipRangeSize(value) > MAX_DISCOVERY_RANGE_SIZE
    ? `The range can have at most ${MAX_DISCOVERY_RANGE_SIZE} addresses (/24).`
    : null;
}
