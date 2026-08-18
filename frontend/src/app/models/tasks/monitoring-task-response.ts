import { TaskType } from './task-type';
import {
  HttpHealthcheckTaskConfigurationResponse,
  PingTaskConfigurationResponse,
  TelnetTaskConfigurationResponse,
} from './monitoring-task-configuration-response';

export interface MonitoringTaskResponse {
  id: number;
  interval: string;
  isEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PingMonitoringTaskResponse extends MonitoringTaskResponse {
  type: TaskType.PING;
  configuration: PingTaskConfigurationResponse;
}

export interface TelnetMonitoringTaskResponse extends MonitoringTaskResponse {
  type: TaskType.TELNET;
  configuration: TelnetTaskConfigurationResponse;
}

export interface HttpHealthcheckMonitoringTaskResponse extends MonitoringTaskResponse {
  type: TaskType.HTTP_HEALTHCHECK;
  configuration: HttpHealthcheckTaskConfigurationResponse;
}
