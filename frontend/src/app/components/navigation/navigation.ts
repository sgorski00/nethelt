import { Component, computed, inject } from '@angular/core';
import { NetworkSelector } from '../networks/network-selector/network-selector';
import { NotificationDropdown } from '../notifications/notification-dropdown/notification-dropdown';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { NetworkContextService } from '../../services/network-context-service';

@Component({
  selector: 'app-navigation',
  imports: [NetworkSelector, NotificationDropdown, RouterLink, RouterLinkActive],
  templateUrl: './navigation.html',
  styleUrl: './navigation.scss',
})
export class Navigation {
  private readonly authService = inject(AuthService);
  private readonly networkContextService = inject(NetworkContextService);
  private readonly router = inject(Router);

  protected readonly isAuthenticated = this.authService.isAuthenticated;
  protected readonly isActiveNetworkSelected = computed(
    () => this.networkContextService.activeNetwork() !== null,
  );

  public logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/login'),
    });
  }
}
