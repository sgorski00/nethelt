import { Routes } from '@angular/router';
import { hasAgentGuard, noAgentGuard } from '../../guards/agent-guard';

export const consoleRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./console').then((m) => m.Console),
    children: [
      {
        path: 'agent',
        canActivate: [hasAgentGuard],
        loadComponent: () => import('./agent/agent').then((m) => m.Agent),
      },
      {
        path: 'agent/create',
        canActivate: [noAgentGuard],
        loadComponent: () => import('./agent/create-agent/create-agent').then((m) => m.CreateAgent),
      },
      {
        path: 'devices',
        loadComponent: () => import('./device/device').then((m) => m.Device),
      },
    ],
  },
];
