import { AgentStatus } from './agent-status';

export interface AgentResponse {
  id: number;
  name: string;
  status: AgentStatus;
  lastHeartbeatAt: string | null;
  tokenCreatedAt: string;
}

export interface AgentTokenResponse {
  token: string;
}
