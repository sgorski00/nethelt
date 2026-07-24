import { Component, inject, OnInit } from '@angular/core';
import { NetworkService } from '../../services/network-service';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Component({
  selector: 'app-networks',
  imports: [RouterLink, RouterOutlet, RouterLinkActive],
  templateUrl: './networks.html',
  styleUrl: './networks.scss',
})
export class Networks implements OnInit {
  private readonly networkService = inject(NetworkService);
  private readonly route = inject(ActivatedRoute);

  protected readonly networks = this.networkService.networks;
  protected readonly message = toSignal(
    this.route.queryParamMap.pipe(
      map((params) => (params.get('deleted') ? 'Network deleted successfully' : '')),
    ),
  );

  ngOnInit(): void {
    this.reloadNetworks();
  }

  protected reloadNetworks() {
    this.networkService.loadNetworks();
  }
}
