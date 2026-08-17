import { TaskType } from './task-type';

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
  port: number;
  path: string;
  timeoutMs: number;
}

export type MonitoringTaskConfigurationRequest =
  | PingTaskConfigurationRequest
  | TelnetTaskConfigurationRequest
  | HttpHealthcheckTaskConfigurationRequest;
