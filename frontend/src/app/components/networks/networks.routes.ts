import { Routes } from '@angular/router';

export const networksRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./networks').then((m) => m.Networks),
  },
];
