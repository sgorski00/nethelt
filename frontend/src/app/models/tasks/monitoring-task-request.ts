import { TaskType } from './task-type';
import { MonitoringTaskConfigurationRequest } from './monitoring-task-configuration-request';

export interface MonitoringTaskCreateRequest {
  type: TaskType;
  intervalSeconds: number;
  configuration: MonitoringTaskConfigurationRequest;
}

export interface MonitoringTaskUpdateRequest {
  intervalSeconds: number;
  configuration: MonitoringTaskConfigurationRequest;
}
