import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from './auth-service';

describe('AuthService', () => {
  const authUrl = `${environment.apiUrl}/auth`;
  let service: AuthService;
  let httpMock: HttpTestingController;

  const setUp = () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  };

  beforeEach(() => localStorage.clear());

  afterEach(() => httpMock.verify());

  it('is authenticated when token is stored', () => {
    localStorage.setItem('token', 'jwt');
    setUp();
    expect(service.isAuthenticated()).toBe(true);
    expect(service.accessToken).toBe('jwt');
  });

  it('saves token after login', () => {
    setUp();
    service.login({ email: 'user@test.com', password: 'secret' }).subscribe();
    httpMock.expectOne(`${authUrl}/login`).flush({ token: 'jwt' });

    expect(service.accessToken).toBe('jwt');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('sends only one refresh request for parallel calls', () => {
    setUp();
    service.refresh().subscribe();
    service.refresh().subscribe();

    httpMock.expectOne(`${authUrl}/refresh`).flush({ token: 'refreshed' });
    expect(service.accessToken).toBe('refreshed');
  });

  it('clears token on logout even if request fails', () => {
    localStorage.setItem('token', 'jwt');
    setUp();
    service.logout().subscribe({ error: () => undefined });
    httpMock.expectOne(`${authUrl}/logout`).flush(null, { status: 500, statusText: 'Error' });

    expect(service.accessToken).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('clears token and redirects to login when unauthorized', () => {
    localStorage.setItem('token', 'jwt');
    setUp();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    service.handleUnauthorized();

    expect(service.isAuthenticated()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
