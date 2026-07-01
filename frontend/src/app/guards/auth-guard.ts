import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service';

function createAuthGuard(requiresAuth: boolean, redirectUrl: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (requiresAuth) {
      return authService.isAuthenticated() ? true : router.createUrlTree([redirectUrl]);
    }

    return authService.isAuthenticated() ? router.createUrlTree([redirectUrl]) : true;
  };
}

export const requireAuth = createAuthGuard(true, '/login');
export const requireNoAuth = (redirectUrl: string) => createAuthGuard(false, redirectUrl);
