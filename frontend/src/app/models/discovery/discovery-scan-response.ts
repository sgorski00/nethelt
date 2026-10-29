import { DiscoveryScanStatus } from './discovery-scan-status';

export interface DiscoveryScanResultResponse {
  ipAddress: string;
  hostname: string | null;
  responseTimeMs: number;
  existingDeviceId: number | null;
}

export interface DiscoveryScanResponse {
  id: number;
  status: DiscoveryScanStatus;
  rangeStart: string | null;
  rangeEnd: string | null;
  requestedAt: string;
  startedAt: string | null;
  finishedAt: string | null;
  failureReason: string | null;
  results: DiscoveryScanResultResponse[];
}

export interface DiscoveryScanStatusResponse {
  networkId: number;
  status: DiscoveryScanStatus;
}
