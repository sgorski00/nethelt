export interface PingTaskConfigurationResponse {
  timeout: string;
}

export interface TelnetTaskConfigurationResponse {
  timeout: string;
  port: number;
}

export interface HttpHealthcheckTaskConfigurationResponse {
  timeout: string;
  port: number;
  path: string;
}

export type MonitoringTaskConfigurationResponse =
  | PingTaskConfigurationResponse
  | TelnetTaskConfigurationResponse
  | HttpHealthcheckTaskConfigurationResponse;
