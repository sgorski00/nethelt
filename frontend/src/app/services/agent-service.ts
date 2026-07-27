import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { NetworkContextService } from './network-context-service';
import { Observable } from 'rxjs';
import { AgentResponse, AgentTokenResponse } from '../models/agent/agent-response';
import {
  AgentCreateRequest,
  AgentStatusUpdateRequest,
  AgentUpdateRequest,
} from '../models/agent/agent-request';

@Injectable({
  providedIn: 'root',
})
export class AgentService {
  private readonly activeNetworkContext = inject(NetworkContextService);
  private readonly httpClient = inject(HttpClient);

  public get networkAgentUrl(): string {
    const networkId = this.activeNetworkContext.getActiveNetworkId();
    return `${environment.apiUrl}/networks/${networkId}/agent`;
  }

  public getAgent(): Observable<AgentResponse> {
    return this.httpClient.get<AgentResponse>(this.networkAgentUrl);
  }

  public create(request: AgentCreateRequest): Observable<AgentTokenResponse> {
    return this.httpClient.post<AgentTokenResponse>(this.networkAgentUrl, request);
  }

  public update(request: AgentUpdateRequest): Observable<AgentResponse> {
    return this.httpClient.patch<AgentResponse>(this.networkAgentUrl, request);
  }

  public delete(): Observable<void> {
    return this.httpClient.delete<void>(this.networkAgentUrl);
  }

  public changeStatus(request: AgentStatusUpdateRequest): Observable<AgentResponse> {
    return this.httpClient.patch<AgentResponse>(`${this.networkAgentUrl}/status`, request);
  }

  public renewToken(): Observable<AgentTokenResponse> {
    return this.httpClient.post<AgentTokenResponse>(`${this.networkAgentUrl}/token`, {});
  }
}
