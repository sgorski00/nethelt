import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  convertToParamMap,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { passwordResetTokenGuard } from './password-reset-token-guard';

describe('passwordResetTokenGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  const run = (queryParams: Record<string, string>) =>
    TestBed.runInInjectionContext(() =>
      passwordResetTokenGuard(
        { queryParamMap: convertToParamMap(queryParams) } as ActivatedRouteSnapshot,
        {} as RouterStateSnapshot,
      ),
    );

  it('allows when token is present', () => {
    expect(run({ token: 'abc' })).toBe(true);
  });

  it('redirects to reset request when token is missing', () => {
    expect((run({}) as UrlTree).toString()).toBe('/password-reset/request');
  });
});
