import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service';

function createAuthGuard(requiresAuth: boolean, redirectUrl: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const authenticated = authService.isAuthenticated();

    return requiresAuth === authenticated ? true : router.createUrlTree([redirectUrl]);
  };
}

export const requireAuth = createAuthGuard(true, '/login');
export const requireNoAuth = (redirectUrl: string) => createAuthGuard(false, redirectUrl);
