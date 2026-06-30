import {UserProfile} from '../../../models/user/user-response';

export interface ProfileDialogData {
  mode: 'create' | 'update';
  profile?: UserProfile;
}
