export enum TaskType {
  PING = 'PING',
  TELNET = 'TELNET',
  HTTP_HEALTHCHECK = 'HTTP_HEALTHCHECK',
}

export const TASK_TYPE_LABELS: Record<TaskType, string> = {
  [TaskType.PING]: 'Ping',
  [TaskType.TELNET]: 'Telnet',
  [TaskType.HTTP_HEALTHCHECK]: 'HTTP Healthcheck',
};
