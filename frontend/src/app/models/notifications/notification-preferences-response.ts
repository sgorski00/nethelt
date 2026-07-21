import { NotificationChannel } from './notification-channels';

export interface NotificationPreferencesResponse {
  userId: number;
  enabledChannels: NotificationChannel[];
}
