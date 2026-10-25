import { AgentConnectionState } from './agent-connection-state';

export interface AgentConnectionStateResponse {
  networkId: number;
  agentId: number;
  connectionState: AgentConnectionState;
}
