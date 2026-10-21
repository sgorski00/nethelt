import { TaskType } from '../../../models/tasks/task-type';

export const SUCCESS_COLOR = '#009e73';
export const FAILURE_COLOR = '#d55e00';

export const TASK_TYPE_COLORS: Record<TaskType, string> = {
  [TaskType.PING]: '#0072b2',
  [TaskType.TELNET]: '#cc79a7',
  [TaskType.HTTP_HEALTHCHECK]: '#e69f00',
};

export const CHART_GRID = { left: 56, right: 24, top: 40, bottom: 32 };

export const CHART_TEXT_STYLE = {
  fontFamily: "'Inter Variable', system-ui, sans-serif",
  color: '#334155',
};

export function formatMs(value: unknown): string {
  return typeof value === 'number' ? `${Math.round(value)} ms` : '-';
}

export function formatInterval(seconds: number): string {
  if (seconds % 86_400 === 0) return seconds === 86_400 ? '1 day' : `${seconds / 86_400} days`;
  if (seconds % 3_600 === 0) return `${seconds / 3_600} h`;
  if (seconds % 60 === 0) return `${seconds / 60} min`;
  return `${seconds} s`;
}
