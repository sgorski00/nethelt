import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const passwordResetTokenGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const token = route.queryParamMap.get('token');

  if (!token) {
    return router.createUrlTree(['/password-reset', 'request']);
  }
  return true;
};
