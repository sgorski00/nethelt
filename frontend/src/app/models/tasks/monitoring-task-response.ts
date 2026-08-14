import { TaskType } from './task-type';

export interface MonitoringTaskResponse {
  id: number;
  type: TaskType;
  interval: string;
  isEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}
