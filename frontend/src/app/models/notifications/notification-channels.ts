export enum NotificationChannel {
  EMAIL = 'EMAIL',
  WEBSOCKET = 'WEBSOCKET',
}

export const NotificationChannelLabels: Record<NotificationChannel, string> = {
  [NotificationChannel.EMAIL]: 'Email',
  [NotificationChannel.WEBSOCKET]: 'Real-time notifications',
};
