import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Client, IMessage, StompConfig } from '@stomp/stompjs';
import { of } from 'rxjs';
import { AuthService } from './auth-service';
import { STOMP_CLIENT, WebSocketService } from './websocket-service';

describe('WebSocketService', () => {
  const isAuthenticated = signal(true);
  const refresh = vi.fn();
  const client = {
    connected: false,
    connectHeaders: {},
    configure: vi.fn(),
    activate: vi.fn(),
    deactivate: vi.fn(),
    subscribe: vi.fn(),
  };
  let service: WebSocketService;

  const config = (): StompConfig => client.configure.mock.calls[0][0];
  const push = (callIndex: number, body: unknown) =>
    client.subscribe.mock.calls[callIndex][1]({ body: JSON.stringify(body) } as IMessage);

  beforeEach(() => {
    isAuthenticated.set(true);
    vi.clearAllMocks();
    client.connected = false;
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
    service = TestBed.inject(WebSocketService);
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

  it('subscribes to watched destinations on connect', () => {
    const received: unknown[] = [];
    service.watch('/user/queue/notifications').subscribe((m) => received.push(m));

    config().onConnect?.({} as never);
    push(0, { id: 1 });

    expect(client.subscribe.mock.calls[0][0]).toBe('/user/queue/notifications');
    expect(received).toEqual([{ id: 1 }]);
  });

  it('subscribes immediately when already connected', () => {
    client.connected = true;

    service.watch('/user/queue/agents').subscribe();

    expect(client.subscribe).toHaveBeenCalledWith('/user/queue/agents', expect.any(Function));
  });

  it('subscribes to a destination only once', () => {
    service.watch('/user/queue/agents').subscribe();
    service.watch('/user/queue/agents').subscribe();

    config().onConnect?.({} as never);

    expect(client.subscribe).toHaveBeenCalledTimes(1);
  });

  it('refreshes the access token when the server rejects the connection', () => {
    config().onStompError?.({} as never);

    expect(refresh).toHaveBeenCalled();
  });
});
