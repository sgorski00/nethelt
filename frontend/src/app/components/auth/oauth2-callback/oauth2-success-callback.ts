import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth-service';
import { NetworkService } from '../../../services/network-service';

@Component({
  selector: 'app-oauth2-success-callback',
  imports: [],
  template: '',
})
export class OAuth2SuccessCallback implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly networkService = inject(NetworkService);

  ngOnInit(): void {
    this.authService.refresh().subscribe({
      next: () => {
        this.networkService.loadNetworks();
        this.router.navigateByUrl('/');
      },
      error: () => this.router.navigateByUrl('/login'),
    });
  }
}
