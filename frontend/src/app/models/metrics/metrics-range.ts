export enum MetricsRange {
  LAST_HOUR = '1h',
  LAST_4_HOURS = '4h',
  LAST_12_HOURS = '12h',
  LAST_DAY = '24h',
  LAST_3_DAYS = '3d',
  LAST_WEEK = '7d',
  CUSTOM = 'custom',
}

export const METRICS_RANGE_LABELS: Record<MetricsRange, string> = {
  [MetricsRange.LAST_HOUR]: 'Last hour',
  [MetricsRange.LAST_4_HOURS]: 'Last 4 hours',
  [MetricsRange.LAST_12_HOURS]: 'Last 12 hours',
  [MetricsRange.LAST_DAY]: 'Last 24 hours',
  [MetricsRange.LAST_3_DAYS]: 'Last 3 days',
  [MetricsRange.LAST_WEEK]: 'Last 7 days',
  [MetricsRange.CUSTOM]: 'Custom',
};

const HOUR_MS = 60 * 60 * 1000;

export const METRICS_RANGE_MS: Record<Exclude<MetricsRange, MetricsRange.CUSTOM>, number> = {
  [MetricsRange.LAST_HOUR]: HOUR_MS,
  [MetricsRange.LAST_4_HOURS]: 4 * HOUR_MS,
  [MetricsRange.LAST_12_HOURS]: 12 * HOUR_MS,
  [MetricsRange.LAST_DAY]: 24 * HOUR_MS,
  [MetricsRange.LAST_3_DAYS]: 3 * 24 * HOUR_MS,
  [MetricsRange.LAST_WEEK]: 7 * 24 * HOUR_MS,
};
