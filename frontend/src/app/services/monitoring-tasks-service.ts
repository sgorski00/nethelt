import { inject, Injectable } from '@angular/core';
import { NetworkContextService } from './network-context-service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { MonitoringTaskResponse } from '../models/tasks/monitoring-task-response';
import {
  MonitoringTaskCreateRequest,
  MonitoringTaskUpdateRequest,
} from '../models/tasks/monitoring-task-request';

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

  public createTask(
    deviceId: number,
    request: MonitoringTaskCreateRequest,
  ): Observable<MonitoringTaskResponse> {
    return this.httpClient.post<MonitoringTaskResponse>(
      `${this.devicesUrl}/${deviceId}/tasks`,
      request,
    );
  }

  public updateTask(deviceId: number, request: MonitoringTaskUpdateRequest, taskId: number) {
    return this.httpClient.put<MonitoringTaskResponse>(
      `${this.devicesUrl}/${deviceId}/tasks/${taskId}`,
      request,
    );
  }

  public disableTask(deviceId: number, taskId: number): Observable<MonitoringTaskResponse> {
    return this.httpClient.patch<MonitoringTaskResponse>(
      `${this.devicesUrl}/${deviceId}/tasks/${taskId}/disable`,
      {},
    );
  }

  public enableTask(deviceId: number, taskId: number): Observable<MonitoringTaskResponse> {
    return this.httpClient.patch<MonitoringTaskResponse>(
      `${this.devicesUrl}/${deviceId}/tasks/${taskId}/enable`,
      {},
    );
  }

  public deleteTask(deviceId: number, taskId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.devicesUrl}/${deviceId}/tasks/${taskId}`);
  }
}
