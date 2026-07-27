import { AgentStatus } from './agent-status';

export interface AgentCreateRequest {
  name: string;
}

export interface AgentUpdateRequest {
  name: string;
}

export interface AgentStatusUpdateRequest {
  status: AgentStatus;
}
