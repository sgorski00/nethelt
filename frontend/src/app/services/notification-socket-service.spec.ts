import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Client, IMessage, StompConfig } from '@stomp/stompjs';
import { of } from 'rxjs';
import { NotificationResponse } from '../models/notifications/notification-response';
import { AuthService } from './auth-service';
import { NotificationSocketService, STOMP_CLIENT } from './notification-socket-service';

describe('NotificationSocketService', () => {
  const isAuthenticated = signal(true);
  const refresh = vi.fn();
  const client = {
    connectHeaders: {},
    configure: vi.fn(),
    activate: vi.fn(),
    deactivate: vi.fn(),
    subscribe: vi.fn(),
  };
  let service: NotificationSocketService;

  const config = (): StompConfig => client.configure.mock.calls[0][0];

  beforeEach(() => {
    isAuthenticated.set(true);
    vi.clearAllMocks();
    client.deactivate.mockResolvedValue(undefined);
    refresh.mockReturnValue(of({ token: 'new' }));
    TestBed.configureTestingModule({
      providers: [
        { provide: STOMP_CLIENT, useValue: client },
        {
          provide: AuthService,
          useValue: { isAuthenticated, accessToken: 'token', refresh },
        },
      ],
    });
    service = TestBed.inject(NotificationSocketService);
    TestBed.tick();
  });

  it('connects when authenticated', () => {
    expect(client.activate).toHaveBeenCalled();
  });

  it('disconnects after logout', () => {
    isAuthenticated.set(false);
    TestBed.tick();

    expect(client.deactivate).toHaveBeenCalled();
  });

  it('sends the access token on connect', () => {
    config().beforeConnect?.(client as unknown as Client);

    expect(client.connectHeaders).toEqual({ Authorization: 'Bearer token' });
  });

  it('emits notifications pushed to the user', () => {
    const notification: NotificationResponse = {
      id: 1,
      title: 'Agent offline',
      content: 'content',
      createdAt: '2026-10-25T12:00:00Z',
      readAt: null,
    };
    const received: NotificationResponse[] = [];
    service.notifications$.subscribe((n) => received.push(n));

    config().onConnect?.({} as never);
    const [destination, callback] = client.subscribe.mock.calls[0];
    callback({ body: JSON.stringify(notification) } as IMessage);

    expect(destination).toBe('/user/queue/notifications');
    expect(received).toEqual([notification]);
  });

  it('refreshes the access token when the server rejects the connection', () => {
    config().onStompError?.({} as never);

    expect(refresh).toHaveBeenCalled();
  });
});
