import { TaskType } from '../tasks/task-type';

export type PingResultExtensionResponse = Record<string, never>;

export interface TelnetResultExtensionResponse {
  portOpen: boolean;
}

export interface HttpHealthcheckResultExtensionResponse {
  statusCode: number | null;
}

export interface MonitoringResultResponse {
  id: number;
  executedAt: string;
  success: boolean;
  message: string | null;
  responseTimeMs: number | null;
  createdAt: string;
}

export interface PingMonitoringResultResponse extends MonitoringResultResponse {
  type: TaskType.PING;
  extension: PingResultExtensionResponse;
}

export interface TelnetMonitoringResultResponse extends MonitoringResultResponse {
  type: TaskType.TELNET;
  extension: TelnetResultExtensionResponse;
}

export interface HttpHealthcheckMonitoringResultResponse extends MonitoringResultResponse {
  type: TaskType.HTTP_HEALTHCHECK;
  extension: HttpHealthcheckResultExtensionResponse;
}
