import { inject, Injectable, signal } from '@angular/core';
import { NetworkResponse } from '../models/network/network-response';
import { NetworkService } from './network-service';

@Injectable({
  providedIn: 'root',
})
export class NetworkContextService {
  private static readonly ACTIVE_NETWORK_ID_KEY = 'activeNetworkId';
  private readonly networkService = inject(NetworkService);

  private readonly activeNetworkState = signal<NetworkResponse | null>(null);
  public readonly activeNetwork = this.activeNetworkState.asReadonly();

  constructor() {
    const activeNetworkId = this.getActiveNetworkId();
    if (activeNetworkId !== null) {
      this.networkService.getNetwork(activeNetworkId).subscribe({
        next: (network) => this.activeNetworkState.set(network),
        error: (error) => {
          console.error('Failed to fetch active network:', error);
          this.clear();
        },
      });
    }
  }

  public setActiveNetwork(network: NetworkResponse): void {
    this.activeNetworkState.set(network);
    localStorage.setItem(NetworkContextService.ACTIVE_NETWORK_ID_KEY, network.id.toString());
  }

  public clear(): void {
    this.activeNetworkState.set(null);
    localStorage.removeItem(NetworkContextService.ACTIVE_NETWORK_ID_KEY);
  }

  public getActiveNetworkId(): number | null {
    const activeNetworkId = localStorage.getItem(NetworkContextService.ACTIVE_NETWORK_ID_KEY);
    return activeNetworkId ? parseInt(activeNetworkId, 10) : null;
  }
}
