import { Routes } from '@angular/router';

export const consoleRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./console').then((m) => m.Console),
    children: [
      {
        path: 'agent',
        loadComponent: () => import('./agent/agent').then((m) => m.Agent),
      },
    ],
  },
];
