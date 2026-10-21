import { Component, computed, inject, signal } from '@angular/core';
import { NetworkSelector } from '../../networks/network-selector/network-selector';
import { NotificationDropdown } from '../../notifications/notification-dropdown/notification-dropdown';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { NetworkContextService } from '../../../services/network-context-service';
import { ThemeService } from '../../../services/theme-service';
import { ThemePreference } from '../../../models/theme/theme';

const THEME_ICONS: Record<ThemePreference, string> = {
  light: 'fa-sun',
  dark: 'fa-moon',
  system: 'fa-circle-half-stroke',
};

const THEME_LABELS: Record<ThemePreference, string> = {
  light: 'Light',
  dark: 'Dark',
  system: 'System',
};

@Component({
  selector: 'app-navigation',
  imports: [NetworkSelector, NotificationDropdown, RouterLink, RouterLinkActive],
  templateUrl: './navigation.html',
  styleUrl: './navigation.scss',
})
export class Navigation {
  private readonly authService = inject(AuthService);
  private readonly networkContextService = inject(NetworkContextService);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  protected readonly isAuthenticated = this.authService.isAuthenticated;
  protected readonly isActiveNetworkSelected = computed(
    () => this.networkContextService.activeNetwork() !== null,
  );
  protected readonly menuOpen = signal(false);
  protected readonly themeIcon = computed(() => THEME_ICONS[this.themeService.preference()]);
  protected readonly themeLabel = computed(
    () => `Theme: ${THEME_LABELS[this.themeService.preference()]} (click to change)`,
  );

  protected cycleTheme() {
    this.themeService.cyclePreference();
  }

  protected toggleMenu() {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenu() {
    this.menuOpen.set(false);
  }

  public logout() {
    this.closeMenu();
    this.authService.logout().subscribe({
      next: () => this.router.navigateByUrl('/login'),
    });
  }
}
