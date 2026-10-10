import { TaskType } from '../tasks/task-type';

export interface MetricsFilters {
  deviceId?: number;
  taskId?: number;
  type?: TaskType;
  from?: string;
  to?: string;
}
