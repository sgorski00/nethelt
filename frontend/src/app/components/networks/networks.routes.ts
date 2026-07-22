import { Routes } from '@angular/router';

export const networksRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./networks').then((m) => m.Networks),
    children: [
      {
        path: 'create',
        loadComponent: () => import('./create-network/create-network').then((m) => m.CreateNetwork),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./network-details/network-details').then((m) => m.NetworkDetails),
      },
    ],
  },
];
