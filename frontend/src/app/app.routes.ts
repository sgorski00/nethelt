import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Profile } from './components/profile/profile';
import {requireAuth, requireNoAuth} from './guards/auth-guard';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
    },
    {
        path: 'login',
        component: Login,
        canActivate: [requireNoAuth('/profile')]
    },
    {
        path: 'profile',
        component: Profile,
        canActivate: [requireAuth]
    }
];
