import { inject, Injectable } from '@angular/core';
import { NetworkContextService } from './network-context-service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { MonitoringTaskResponse } from '../models/tasks/monitoring-task-response';

@Injectable({
  providedIn: 'root',
})
export class MonitoringTasksService {
  private readonly activeNetworkContext = inject(NetworkContextService);
  private readonly httpClient = inject(HttpClient);

  public get devicesUrl(): string {
    const networkId = this.activeNetworkContext.getActiveNetworkId();
    return `${environment.apiUrl}/networks/${networkId}/devices`;
  }

  public getTasks(deviceId: number): Observable<MonitoringTaskResponse[]> {
    return this.httpClient.get<MonitoringTaskResponse[]>(`${this.devicesUrl}/${deviceId}/tasks`);
  }
}
