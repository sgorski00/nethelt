import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { AuthService } from '../services/auth-service';
import { requireAuth, requireNoAuth } from './auth-guard';

describe('auth guards', () => {
  let authenticated: boolean;

  beforeEach(() => {
    authenticated = false;
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAuthenticated: () => authenticated } },
      ],
    });
  });

  const run = (guard: typeof requireAuth) =>
    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  it('requireAuth allows authenticated user', () => {
    authenticated = true;
    expect(run(requireAuth)).toBe(true);
  });

  it('requireAuth redirects anonymous user to /login', () => {
    const result = run(requireAuth) as UrlTree;
    expect(result.toString()).toBe('/login');
  });

  it('requireNoAuth allows anonymous user', () => {
    expect(run(requireNoAuth('/profile'))).toBe(true);
  });

  it('requireNoAuth redirects authenticated user', () => {
    authenticated = true;
    const result = run(requireNoAuth('/profile')) as UrlTree;
    expect(result.toString()).toBe('/profile');
  });
});
