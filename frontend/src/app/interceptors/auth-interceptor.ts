import { HttpInterceptorFn } from '@angular/common/http';

const LS_TOKEN_KEY = 'token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(LS_TOKEN_KEY);
  if (!token) {
    return next(req);
  }

  const cloned = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(cloned);
};
