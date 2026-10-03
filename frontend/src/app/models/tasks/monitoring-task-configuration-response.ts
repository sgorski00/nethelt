import { HttpScheme } from './http-scheme';

export interface PingTaskConfigurationResponse {
  timeout: string;
}

export interface TelnetTaskConfigurationResponse {
  timeout: string;
  port: number;
}

export interface HttpHealthcheckTaskConfigurationResponse {
  timeout: string;
  scheme: HttpScheme;
  port: number;
  path: string;
  host: string | null;
  expectedStatusCode: number | null;
}

export type MonitoringTaskConfigurationResponse =
  | PingTaskConfigurationResponse
  | TelnetTaskConfigurationResponse
  | HttpHealthcheckTaskConfigurationResponse;
