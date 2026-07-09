export const PASSWORD_DIALOG_MODE = {
  SET: 'set',
  CHANGE: 'change',
} as const;

export type PasswordDialogMode = (typeof PASSWORD_DIALOG_MODE)[keyof typeof PASSWORD_DIALOG_MODE];

export interface PasswordDialogData {
  mode: PasswordDialogMode;
}
