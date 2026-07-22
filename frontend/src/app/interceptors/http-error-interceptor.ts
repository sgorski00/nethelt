import { HttpErrorResponse, HttpEvent, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { catchError, Observable, switchMap, throwError } from 'rxjs';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        return handle401(authService, req, error, next);
      }
      return throwError(() => error);
    }),
  );
};

function handle401(
  authService: AuthService,
  req: HttpRequest<unknown>,
  error: HttpErrorResponse,
  next: (req: HttpRequest<unknown>) => Observable<HttpEvent<unknown>>,
) {
  if (req.url.includes('/auth/')) {
    authService.handleUnauthorized();
    return throwError(() => error);
  }

  return authService.refresh().pipe(
    switchMap((_) => {
      const token = authService.accessToken;
      const cloned = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
      return next(cloned);
    }),
    catchError((refreshError) => {
      authService.handleUnauthorized();
      return throwError(() => refreshError);
    }),
  );
}
