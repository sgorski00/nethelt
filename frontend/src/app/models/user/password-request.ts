export interface PasswordSetRequest {
  newPassword: string;
  repeatNewPassword: string;
}

export interface PasswordChangeRequest {
  oldPassword: string;
  newPassword: string;
  repeatNewPassword: string;
}
