import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NetworkContextService } from './network-context-service';
import { Observable } from 'rxjs';
import { DeviceResponse } from '../models/device/device-response';
import { PageResponse } from '../models/general/page-response';
import { DeviceCreateRequest, DeviceUpdateRequest } from '../models/device/device-request';
import { DeviceType } from '../models/device/device-type';

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

  public getDevices(
    type?: DeviceType,
    sort = 'createdAt,desc',
    page = 0,
    size = 20,
  ): Observable<PageResponse<DeviceResponse>> {
    let params = new HttpParams()
      .set('sort', sort)
      .set('page', page.toString())
      .set('size', size.toString());
    if (type) params = params.set('type', type);

    return this.httpClient.get<PageResponse<DeviceResponse>>(this.devicesUrl, { params });
  }

  public createDevice(request: DeviceCreateRequest): Observable<DeviceResponse> {
    return this.httpClient.post<DeviceResponse>(this.devicesUrl, request);
  }

  public updateDevice(request: DeviceUpdateRequest, deviceId: number): Observable<DeviceResponse> {
    return this.httpClient.put<DeviceResponse>(`${this.devicesUrl}/${deviceId}`, request);
  }

  public enableDevice(deviceId: number): Observable<void> {
    return this.httpClient.patch<void>(`${this.devicesUrl}/${deviceId}/enable`, {});
  }

  public disableDevice(deviceId: number): Observable<void> {
    return this.httpClient.patch<void>(`${this.devicesUrl}/${deviceId}/disable`, {});
  }

  public deleteDevice(deviceId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.devicesUrl}/${deviceId}`);
  }
}
