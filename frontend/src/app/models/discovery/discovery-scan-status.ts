export enum DiscoveryScanStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  TIMED_OUT = 'TIMED_OUT',
}

export const DISCOVERY_SCAN_STATUS_LABELS: Record<DiscoveryScanStatus, string> = {
  [DiscoveryScanStatus.PENDING]: 'Waiting for agent',
  [DiscoveryScanStatus.RUNNING]: 'Scanning',
  [DiscoveryScanStatus.COMPLETED]: 'Completed',
  [DiscoveryScanStatus.FAILED]: 'Failed',
  [DiscoveryScanStatus.TIMED_OUT]: 'Timed out',
};

export function isScanInProgress(status: DiscoveryScanStatus): boolean {
  return status === DiscoveryScanStatus.PENDING || status === DiscoveryScanStatus.RUNNING;
}
