import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { NetworkContextService } from './network-context-service';
import { DiscoveryScanResponse } from '../models/discovery/discovery-scan-response';
import { DiscoveryScanStartRequest } from '../models/discovery/discovery-scan-request';

@Injectable({
  providedIn: 'root',
})
export class DiscoveryScanService {
  private readonly activeNetworkContext = inject(NetworkContextService);
  private readonly httpClient = inject(HttpClient);

  public get discoveryScanUrl(): string {
    const networkId = this.activeNetworkContext.getActiveNetworkId();
    return `${environment.apiUrl}/networks/${networkId}/discovery-scan`;
  }

  public getScan(): Observable<DiscoveryScanResponse | null> {
    return this.httpClient
      .get<DiscoveryScanResponse>(this.discoveryScanUrl)
      .pipe(catchError((err) => (err.status === 404 ? of(null) : throwError(() => err))));
  }

  public startScan(request: DiscoveryScanStartRequest): Observable<DiscoveryScanResponse> {
    return this.httpClient.post<DiscoveryScanResponse>(this.discoveryScanUrl, request);
  }
}
