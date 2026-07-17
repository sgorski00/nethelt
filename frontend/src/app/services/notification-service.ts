import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  NotificationChannel,
  NotificationPreferencesResponse,
} from '../models/notifications/notification-preferences-response';
import { environment } from '../../environments/environment';

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
}
