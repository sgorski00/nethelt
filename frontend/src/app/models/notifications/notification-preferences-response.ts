export enum NotificationChannel {
  EMAIL = 'EMAIL',
  WEBSOCKET = 'WEBSOCKET',
}

export interface NotificationPreferencesResponse {
  userId: number;
  enabledChannels: NotificationChannel[];
}
