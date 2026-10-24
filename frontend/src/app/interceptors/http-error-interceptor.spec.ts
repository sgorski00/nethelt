import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';
import { httpErrorInterceptor } from './http-error-interceptor';

describe('httpErrorInterceptor', () => {
  const authService = {
    accessToken: 'new-token',
    refresh: vi.fn(),
    handleUnauthorized: vi.fn(),
  };
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    authService.refresh.mockReset();
    authService.handleUnauthorized.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([httpErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  const unauthorized = { status: 401, statusText: 'Unauthorized' };

  it('refreshes token and retries request on 401', () => {
    authService.refresh.mockReturnValue(of({ token: 'new-token' }));
    const next = vi.fn();
    http.get('/api/devices').subscribe(next);

    httpMock.expectOne('/api/devices').flush(null, unauthorized);
    const retry = httpMock.expectOne('/api/devices');
    expect(retry.request.headers.get('Authorization')).toBe('Bearer new-token');
    retry.flush({ ok: true });

    expect(next).toHaveBeenCalledWith({ ok: true });
    expect(authService.handleUnauthorized).not.toHaveBeenCalled();
  });

  it('logs out when refresh fails', () => {
    authService.refresh.mockReturnValue(throwError(() => new Error('refresh failed')));
    const error = vi.fn();
    http.get('/api/devices').subscribe({ error });

    httpMock.expectOne('/api/devices').flush(null, unauthorized);

    expect(authService.handleUnauthorized).toHaveBeenCalled();
    expect(error).toHaveBeenCalled();
  });

  it('does not refresh on 401 from auth endpoints', () => {
    const error = vi.fn();
    http.post('/api/auth/login', {}).subscribe({ error });

    httpMock.expectOne('/api/auth/login').flush(null, unauthorized);

    expect(authService.refresh).not.toHaveBeenCalled();
    expect(authService.handleUnauthorized).toHaveBeenCalled();
    expect(error).toHaveBeenCalled();
  });

  it('passes other errors through', () => {
    const error = vi.fn();
    http.get('/api/devices').subscribe({ error });

    httpMock.expectOne('/api/devices').flush(null, { status: 500, statusText: 'Server Error' });

    expect(authService.refresh).not.toHaveBeenCalled();
    expect(error.mock.calls[0][0].status).toBe(500);
  });
});
