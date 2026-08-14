import { TaskType } from './task-type';

export interface MonitoringTaskCreateRequest {
  type: TaskType;
  intervalSeconds: number;
}

export interface MonitoringTaskUpdateRequest {
  intervalSeconds: number;
}
