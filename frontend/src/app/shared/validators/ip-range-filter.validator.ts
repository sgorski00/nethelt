import { IPV4_REGEX } from './ip.validator';

export const MAX_IP_RANGES = 20;

/**
 * Validates a comma-separated IPv4 filter, e.g. `192.168.0.0/24, 10.0.0.1-10.0.0.50, 10.0.0.5`.
 * Returns an error message, or null when the filter is valid.
 */
export function validateIpRangeFilter(value: string): string | null {
  const tokens = value.split(',').map((token) => token.trim());

  if (tokens.some((token) => token === '')) return 'Filter contains an empty value.';
  if (tokens.length > MAX_IP_RANGES) return `Filter can contain at most ${MAX_IP_RANGES} values.`;

  for (const token of tokens) {
    const error = validateToken(token);
    if (error) return `'${token}': ${error}`;
  }
  return null;
}

function validateToken(token: string): string | null {
  if (token.includes('-')) {
    const parts = token.split('-').map((part) => part.trim());
    if (parts.length !== 2) return "range must have exactly one '-'.";
    const [start, end] = parts;
    if (!IPV4_REGEX.test(start)) return `invalid IP address '${start}'.`;
    if (!IPV4_REGEX.test(end)) return `invalid IP address '${end}'.`;
    if (toNumber(start) > toNumber(end)) return 'range start is greater than its end.';
    return null;
  }

  if (token.includes('/')) {
    const parts = token.split('/').map((part) => part.trim());
    if (parts.length !== 2) return "subnet must have exactly one '/'.";
    const [address, prefix] = parts;
    if (!IPV4_REGEX.test(address)) return `invalid IP address '${address}'.`;
    if (!/^\d{1,2}$/.test(prefix) || Number(prefix) > 32) {
      return 'subnet mask must be a number from 0 to 32.';
    }
    return null;
  }

  return IPV4_REGEX.test(token) ? null : 'invalid IP address.';
}

function toNumber(address: string): number {
  return address.split('.').reduce((result, octet) => result * 256 + Number(octet), 0);
}
