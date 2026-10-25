import { DestroyRef, effect, inject, Injectable, InjectionToken } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth-service';

export const STOMP_CLIENT = new InjectionToken<Client>('STOMP_CLIENT', {
  providedIn: 'root',
  factory: () => new Client(),
});

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private readonly authService = inject(AuthService);
  private readonly client = inject(STOMP_CLIENT);
  private readonly destinations = new Map<string, Subject<unknown>>();

  constructor() {
    this.client.configure({
      brokerURL: environment.wsUrl,
      reconnectDelay: 5000,
      beforeConnect: (client) => {
        client.connectHeaders = { Authorization: `Bearer ${this.authService.accessToken}` };
      },
      onConnect: () =>
        this.destinations.forEach((subject, destination) => this.subscribe(destination, subject)),
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

  public watch<T>(destination: string): Observable<T> {
    let subject = this.destinations.get(destination);
    if (!subject) {
      subject = new Subject<unknown>();
      this.destinations.set(destination, subject);
      if (this.client.connected) {
        this.subscribe(destination, subject);
      }
    }
    return subject.asObservable() as Observable<T>;
  }

  private subscribe(destination: string, subject: Subject<unknown>) {
    this.client.subscribe(destination, (message) => subject.next(JSON.parse(message.body)));
  }
}
