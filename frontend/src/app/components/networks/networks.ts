import { Component, inject } from '@angular/core';
import { NetworkService } from '../../services/network-service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-networks',
  imports: [],
  templateUrl: './networks.html',
  styleUrl: './networks.scss',
})
export class Networks {
  private readonly networkService = inject(NetworkService);

  protected readonly networks = toSignal(this.networkService.getNetworks());
}
