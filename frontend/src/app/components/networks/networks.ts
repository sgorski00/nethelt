import { Component, inject, OnInit } from '@angular/core';
import { NetworkService } from '../../services/network-service';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-networks',
  imports: [RouterLink, RouterOutlet],
  templateUrl: './networks.html',
  styleUrl: './networks.scss',
})
export class Networks implements OnInit {
  private readonly networkService = inject(NetworkService);

  protected readonly networks = this.networkService.networks;

  ngOnInit(): void {
    this.reloadNetworks();
  }

  protected reloadNetworks() {
    this.networkService.loadNetworks();
  }
}
