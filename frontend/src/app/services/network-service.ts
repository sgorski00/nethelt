import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { NetworkResponse } from '../models/network/network-response';
import { NetworkRequest } from '../models/network/network-request';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NetworkService {
  private readonly networksUrl = `${environment.apiUrl}/networks`;
  private readonly httpClient = inject(HttpClient);
  private readonly networksCache = signal([] as NetworkResponse[]);
  public readonly networks = this.networksCache.asReadonly();

  public loadNetworks() {
    return this.httpClient
      .get<NetworkResponse[]>(`${this.networksUrl}`)
      .pipe(tap((networks) => this.networksCache.set(networks)));
  }

  public getNetwork(id: number) {
    return this.httpClient.get<NetworkResponse>(`${this.networksUrl}/${id}`);
  }

  public createNetwork(request: NetworkRequest) {
    return this.httpClient
      .post<NetworkResponse>(`${this.networksUrl}`, request)
      .pipe(tap((network) => this.addNetworkToCache(network)));
  }

  public updateNetwork(id: number, request: NetworkRequest) {
    return this.httpClient.put<NetworkResponse>(`${this.networksUrl}/${id}`, request).pipe(
      tap((network) => {
        this.removeNetworkFromCache(id);
        this.addNetworkToCache(network);
      }),
    );
  }

  public deleteNetwork(id: number) {
    return this.httpClient
      .delete<void>(`${this.networksUrl}/${id}`)
      .pipe(tap(() => this.removeNetworkFromCache(id)));
  }

  public addNetworkToCache(network: NetworkResponse) {
    this.networksCache.update((networks) => [...networks, network]);
  }

  public removeNetworkFromCache(id: number) {
    this.networksCache.update((networks) => networks.filter((network) => network.id !== id));
  }
}
