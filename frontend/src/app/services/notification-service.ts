import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { NotificationPreferencesResponse } from '../models/notifications/notification-preferences-response';
import { environment } from '../../environments/environment';
import { NotificationResponse } from '../models/notifications/notification-response';
import { NotificationChannel } from '../models/notifications/notification-channels';
import { PageResponse } from '../models/general/page-response';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly notificationsUrl = `${environment.apiUrl}/notifications`;
  private readonly httpClient = inject(HttpClient);

  public getPreferences(): Observable<NotificationPreferencesResponse> {
    return this.httpClient.get<NotificationPreferencesResponse>(
      `${this.notificationsUrl}/preferences`,
    );
  }

  public enableChannel(channel: NotificationChannel): Observable<NotificationPreferencesResponse> {
    return this.httpClient.put<NotificationPreferencesResponse>(
      `${this.notificationsUrl}/preferences/${channel}`,
      {},
    );
  }

  public disableChannel(channel: NotificationChannel): Observable<NotificationPreferencesResponse> {
    return this.httpClient.delete<NotificationPreferencesResponse>(
      `${this.notificationsUrl}/preferences/${channel}`,
      {},
    );
  }

  public getNotifications(
    page = 0,
    showRead = true,
    size = 5,
    sort = 'createdAt,desc',
  ): Observable<PageResponse<NotificationResponse>> {
    const params = { page, size, sort, showRead };
    return this.httpClient.get<PageResponse<NotificationResponse>>(`${this.notificationsUrl}`, {
      params,
    });
  }

  public getUnreadCount(): Observable<number> {
    return this.httpClient.get<number>(`${this.notificationsUrl}/unread-count`);
  }

  public markAsRead(notificationId: number): Observable<void> {
    return this.httpClient.put<void>(`${this.notificationsUrl}/${notificationId}`, {});
  }
}
