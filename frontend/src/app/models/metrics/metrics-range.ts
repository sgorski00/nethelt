export enum MetricsRange {
  LAST_HOUR = '1h',
  LAST_DAY = '24h',
  LAST_WEEK = '7d',
  CUSTOM = 'custom',
}

export const METRICS_RANGE_LABELS: Record<MetricsRange, string> = {
  [MetricsRange.LAST_HOUR]: 'Last hour',
  [MetricsRange.LAST_DAY]: 'Last 24 hours',
  [MetricsRange.LAST_WEEK]: 'Last 7 days',
  [MetricsRange.CUSTOM]: 'Custom',
};

export const METRICS_RANGE_MS: Record<Exclude<MetricsRange, MetricsRange.CUSTOM>, number> = {
  [MetricsRange.LAST_HOUR]: 60 * 60 * 1000,
  [MetricsRange.LAST_DAY]: 24 * 60 * 60 * 1000,
  [MetricsRange.LAST_WEEK]: 7 * 24 * 60 * 60 * 1000,
};
