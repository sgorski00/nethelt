import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Profile } from './components/profile/profile';
import { requireAuth, requireNoAuth } from './guards/auth-guard';
import { OAuth2SuccessCallback } from './components/oauth2-callback/oauth2-success-callback';
import { Register } from './components/register/register';

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
    path: 'oauth2/success',
    component: OAuth2SuccessCallback,
  },
];
