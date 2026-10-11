import { AgentConnectionState } from './agent-connection-state';
import { AgentStatus } from './agent-status';

export interface AgentResponse {
  id: number;
  name: string;
  status: AgentStatus;
  connectionState: AgentConnectionState;
  lastHeartbeatAt: string | null;
  tokenCreatedAt: string;
  configuration: AgentConfigurationResponse;
}

export interface AgentConfigurationResponse {
  maxUnavailabilitySeconds: number;
}

export interface AgentTokenResponse {
  token: string;
}
