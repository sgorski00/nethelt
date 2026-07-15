import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
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
