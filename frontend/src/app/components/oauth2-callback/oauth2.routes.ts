import { Routes } from '@angular/router';
import { OAuth2SuccessCallback } from './oauth2-success-callback';
import { OAuth2FailureCallback } from './oauth2-failure-callback';

export const oauth2Routes: Routes = [
  {
    path: 'success',
    component: OAuth2SuccessCallback,
  },
  {
    path: 'failure',
    component: OAuth2FailureCallback,
  },
];
