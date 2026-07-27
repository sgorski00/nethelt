import { inject, Injectable, signal } from '@angular/core';
import { NetworkResponse } from '../models/network/network-response';
import { NetworkService } from './network-service';

@Injectable({
  providedIn: 'root',
})
export class NetworkContextService {
  private static readonly ACTIVE_NETWORK_ID_KEY = 'activeNetworkId';
  private readonly networkService = inject(NetworkService);
  private readonly initialized = signal(false);

  private readonly activeNetworkState = signal<NetworkResponse | null>(null);
  public readonly activeNetwork = this.activeNetworkState.asReadonly();
  public readonly isChanged = signal(0);

  public load() {
    if (this.initialized()) {
      return;
    }
    this.initialized.set(true);
    const activeNetworkId = this.getActiveNetworkId();
    if (activeNetworkId === null) {
      return;
    }
    this.networkService.getNetwork(activeNetworkId).subscribe({
      next: (network) => this.activeNetworkState.set(network),
      error: () => this.clear(),
    });
  }

  public setActiveNetwork(network: NetworkResponse): void {
    this.isChanged.update((value) => value + 1);
    this.activeNetworkState.set(network);
    localStorage.setItem(NetworkContextService.ACTIVE_NETWORK_ID_KEY, network.id.toString());
  }

  public clear(): void {
    this.isChanged.set(0);
    this.activeNetworkState.set(null);
    localStorage.removeItem(NetworkContextService.ACTIVE_NETWORK_ID_KEY);
  }

  public getActiveNetworkId(): number | null {
    const activeNetworkId = localStorage.getItem(NetworkContextService.ACTIVE_NETWORK_ID_KEY);
    return activeNetworkId ? parseInt(activeNetworkId, 10) : null;
  }
}
