import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { NetworkResponse } from '../models/network/network-response';
import { NetworkRequest } from '../models/network/network-request';

@Injectable({
  providedIn: 'root',
})
export class NetworkService {
  private readonly networksUrl = `${environment.apiUrl}/networks`;
  private readonly httpClient = inject(HttpClient);

  public getNetworks() {
    return this.httpClient.get<NetworkResponse[]>(`${this.networksUrl}`);
  }

  public createNetwork(request: NetworkRequest) {
    return this.httpClient.post<NetworkResponse>(`${this.networksUrl}`, request);
  }
}
