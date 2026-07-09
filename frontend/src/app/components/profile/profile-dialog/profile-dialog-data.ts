import { UserProfile } from '../../../models/user/user-response';

export const PROFILE_DIALOG_MODE = {
  CREATE: 'create',
  UPDATE: 'update',
} as const;

export type ProfileDialogMode = (typeof PROFILE_DIALOG_MODE)[keyof typeof PROFILE_DIALOG_MODE];

export interface ProfileDialogData {
  mode: ProfileDialogMode;
  profile?: UserProfile;
}
