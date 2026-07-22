import { Routes } from '@angular/router';
import { requireAuth, requireNoAuth } from './guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login').then((m) => m.Login),
    canActivate: [requireNoAuth('/profile')],
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register').then((m) => m.Register),
    canActivate: [requireNoAuth('/profile')],
  },
  {
    path: 'password-reset',
    loadChildren: () =>
      import('./components/auth/password-reset/password-reset.routes').then(
        (m) => m.passwordResetRoutes,
      ),
    canActivate: [requireNoAuth('/profile')],
  },
  {
    path: 'profile',
    loadComponent: () => import('./components/profile/profile').then((m) => m.Profile),
    canActivate: [requireAuth],
  },
  {
    path: 'notifications/preferences',
    loadComponent: () =>
      import('./components/notifications/notification-preferences-dialog/notification-preferences-dialog').then(
        (m) => m.NotificationPreferencesDialog,
      ),
    canActivate: [requireAuth],
  },
  {
    path: 'oauth2',
    loadChildren: () =>
      import('./components/auth/oauth2-callback/oauth2.routes').then((m) => m.oauth2Routes),
  },
  {
    path: 'networks',
    loadChildren: () =>
      import('./components/networks/networks.routes').then((m) => m.networksRoutes),
    canActivate: [requireAuth],
  },
];
