import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { NetworkContextService } from './network-context-service';
import { MetricsFilters } from '../models/metrics/metrics-filters';
import { MetricsResponse } from '../models/metrics/metrics-response';

@Injectable({
  providedIn: 'root',
})
export class MetricsService {
  private readonly activeNetworkContext = inject(NetworkContextService);
  private readonly httpClient = inject(HttpClient);

  public get metricsUrl(): string {
    const networkId = this.activeNetworkContext.getActiveNetworkId();
    return `${environment.apiUrl}/networks/${networkId}/metrics`;
  }

  public getMetrics(filters: MetricsFilters): Observable<MetricsResponse> {
    let params = new HttpParams();
    if (filters.deviceId !== undefined) params = params.set('deviceId', filters.deviceId);
    if (filters.taskId !== undefined) params = params.set('taskId', filters.taskId);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.from) params = params.set('from', filters.from);
    if (filters.to) params = params.set('to', filters.to);

    return this.httpClient.get<MetricsResponse>(this.metricsUrl, { params });
  }
}
