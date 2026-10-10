import { TaskType } from '../tasks/task-type';

export interface LatencyStatsResponse {
  type: TaskType;
  total: number;
  avgMs: number | null;
  p95Ms: number | null;
}

export interface MetricsSummaryResponse {
  total: number;
  successful: number;
  failed: number;
  uptimePercent: number | null;
  latency: LatencyStatsResponse[];
}

export interface AvailabilityPointResponse {
  bucketStart: string;
  total: number;
  successful: number;
}

export interface LatencyPointResponse {
  bucketStart: string;
  type: TaskType;
  avgMs: number | null;
  p95Ms: number | null;
}

export interface TaskMetricsResponse {
  deviceId: number;
  deviceName: string;
  taskId: number;
  type: TaskType;
  total: number;
  successful: number;
  uptimePercent: number;
  avgMs: number | null;
}

export interface MetricsResponse {
  from: string;
  to: string;
  bucketSeconds: number;
  summary: MetricsSummaryResponse;
  availability: AvailabilityPointResponse[];
  latency: LatencyPointResponse[];
  breakdown: TaskMetricsResponse[];
}
