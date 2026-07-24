import { Component, inject } from '@angular/core';
import { NetworkService } from '../../../services/network-service';
import { NetworkContextService } from '../../../services/network-context-service';

@Component({
  selector: 'app-network-selector',
  imports: [],
  templateUrl: './network-selector.html',
  styleUrl: './network-selector.scss',
})
export class NetworkSelector {
  private readonly networkService = inject(NetworkService);
  private readonly networkContext = inject(NetworkContextService);

  readonly networks = this.networkService.networks;
  readonly activeNetwork = this.networkContext.activeNetwork;

  selectNetwork(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);

    const network = this.networks().find((network) => network.id === id);

    if (network) {
      this.networkContext.setActiveNetwork(network);
    }
  }
}
