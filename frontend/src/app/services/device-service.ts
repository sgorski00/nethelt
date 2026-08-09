import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { NetworkContextService } from './network-context-service';
import { Observable } from 'rxjs';
import { DeviceResponse } from '../models/device/device-response';
import { PageResponse } from '../models/general/page-response';
import { DeviceCreateRequest, DeviceUpdateRequest } from '../models/device/device-request';

@Injectable({
  providedIn: 'root',
})
export class DeviceService {
  private readonly activeNetworkContext = inject(NetworkContextService);
  private readonly httpClient = inject(HttpClient);

  public get devicesUrl(): string {
    const networkId = this.activeNetworkContext.getActiveNetworkId();
    return `${environment.apiUrl}/networks/${networkId}/devices`;
  }

  public getDevices(): Observable<PageResponse<DeviceResponse>> {
    return this.httpClient.get<PageResponse<DeviceResponse>>(this.devicesUrl);
  }

  public createDevice(request: DeviceCreateRequest): Observable<DeviceResponse> {
    return this.httpClient.post<DeviceResponse>(this.devicesUrl, request);
  }

  public updateDevice(request: DeviceUpdateRequest, deviceId: number): Observable<DeviceResponse> {
    return this.httpClient.put<DeviceResponse>(`${this.devicesUrl}/${deviceId}`, request);
  }
}
