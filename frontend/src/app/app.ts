import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth-service';
import { NotificationDropdown } from './components/notifications/notification-dropdown/notification-dropdown';
import { NetworkSelector } from './components/networks/network-selector/network-selector';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, NotificationDropdown, NetworkSelector],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly title = signal('nethelt-frontend');

  public readonly currentYear = new Date().getFullYear();

  public logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/login'),
    });
  }

  public isAuthenticated() {
    return this.authService.isAuthenticated();
  }
}
