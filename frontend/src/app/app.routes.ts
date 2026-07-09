import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Profile } from './components/profile/profile';
import { requireAuth, requireNoAuth } from './guards/auth-guard';
import { Register } from './components/register/register';
import { oauth2Routes } from './components/oauth2-callback/oauth2.routes';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    component: Login,
    canActivate: [requireNoAuth('/profile')],
  },
  {
    path: 'register',
    component: Register,
    canActivate: [requireNoAuth('/profile')],
  },
  {
    path: 'profile',
    component: Profile,
    canActivate: [requireAuth],
  },
  {
    path: 'oauth2',
    children: oauth2Routes,
  },
];
