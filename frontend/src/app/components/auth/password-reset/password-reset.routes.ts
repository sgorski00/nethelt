import { Routes } from '@angular/router';
import { PasswordReset } from './request/password-reset';
import { PasswordResetConfirm } from './confirm/password-reset-confirm';
import { passwordResetTokenGuard } from '../../../guards/password-reset-token-guard';

export const passwordResetRoutes: Routes = [
  {
    path: 'request',
    component: PasswordReset,
  },
  {
    path: 'confirm',
    component: PasswordResetConfirm,
    canActivate: [passwordResetTokenGuard],
  },
];
