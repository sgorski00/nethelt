import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navigation } from './components/shared/navigation/navigation';
import { NetworkContextService } from './services/network-context-service';
import { NetworkService } from './services/network-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navigation],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly networkContext = inject(NetworkContextService);
  private readonly networkService = inject(NetworkService);
  protected readonly title = signal('nethelt-frontend');

  public readonly currentYear = new Date().getFullYear();

  constructor() {
    this.networkService.loadNetworks();
    this.networkContext.load();
  }
}
