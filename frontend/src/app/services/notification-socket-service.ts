import { DestroyRef, effect, inject, Injectable, InjectionToken } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { NotificationResponse } from '../models/notifications/notification-response';
import { AuthService } from './auth-service';

export const STOMP_CLIENT = new InjectionToken<Client>('STOMP_CLIENT', {
  providedIn: 'root',
  factory: () => new Client(),
});

@Injectable({
  providedIn: 'root',
})
export class NotificationSocketService {
  private static readonly DESTINATION = '/user/queue/notifications';
  private readonly authService = inject(AuthService);
  private readonly client = inject(STOMP_CLIENT);
  private readonly received = new Subject<NotificationResponse>();
  public readonly notifications$ = this.received.asObservable();

  constructor() {
    this.client.configure({
      brokerURL: environment.wsUrl,
      reconnectDelay: 5000,
      beforeConnect: (client) => {
        client.connectHeaders = { Authorization: `Bearer ${this.authService.accessToken}` };
      },
      onConnect: () => {
        this.client.subscribe(NotificationSocketService.DESTINATION, (message) =>
          this.received.next(JSON.parse(message.body)),
        );
      },
      onStompError: () => this.authService.refresh().subscribe({ error: () => undefined }),
    });

    effect(() => {
      if (this.authService.isAuthenticated()) {
        this.client.activate();
      } else {
        void this.client.deactivate();
      }
    });
    inject(DestroyRef).onDestroy(() => void this.client.deactivate());
  }
}
