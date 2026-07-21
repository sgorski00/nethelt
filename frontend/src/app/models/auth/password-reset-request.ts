export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetConfirmRequest {
  newPassword: string;
  repeatNewPassword: string;
}
