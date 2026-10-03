import { TaskType } from './task-type';
import { HttpScheme } from './http-scheme';

export interface PingTaskConfigurationRequest {
  type: TaskType.PING;
  timeoutMs: number;
}

export interface TelnetTaskConfigurationRequest {
  type: TaskType.TELNET;
  port: number;
  timeoutMs: number;
}

export interface HttpHealthcheckTaskConfigurationRequest {
  type: TaskType.HTTP_HEALTHCHECK;
  scheme: HttpScheme;
  port: number;
  path: string;
  host: string | null;
  expectedStatusCode: number | null;
  timeoutMs: number;
}

export type MonitoringTaskConfigurationRequest =
  | PingTaskConfigurationRequest
  | TelnetTaskConfigurationRequest
  | HttpHealthcheckTaskConfigurationRequest;
