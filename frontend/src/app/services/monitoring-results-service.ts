import { inject, Injectable } from '@angular/core';
import { NetworkContextService } from './network-context-service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/general/page-response';
import {
  HttpHealthcheckMonitoringResultResponse,
  PingMonitoringResultResponse,
  TelnetMonitoringResultResponse,
} from '../models/results/monitoring-result-response';

@Injectable({
  providedIn: 'root',
})
export class MonitoringResultsService {
  private readonly activeNetworkContext = inject(NetworkContextService);
  private readonly httpClient = inject(HttpClient);

  public get devicesUrl(): string {
    const networkId = this.activeNetworkContext.getActiveNetworkId();
    return `${environment.apiUrl}/networks/${networkId}/devices`;
  }

  public getResults(
    deviceId: number,
    taskId: number,
    from?: string,
    to?: string,
    page = 0,
    size = 20,
  ): Observable<
    PageResponse<
      | PingMonitoringResultResponse
      | TelnetMonitoringResultResponse
      | HttpHealthcheckMonitoringResultResponse
    >
  > {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return this.httpClient.get<
      PageResponse<
        | PingMonitoringResultResponse
        | TelnetMonitoringResultResponse
        | HttpHealthcheckMonitoringResultResponse
      >
    >(`${this.devicesUrl}/${deviceId}/tasks/${taskId}/results`, { params });
  }
}
