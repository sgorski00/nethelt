import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { PreloadAllModules, provideRouter, withPreloading } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth-interceptor';
import { httpErrorInterceptor } from './interceptors/http-error-interceptor';

import { provideEchartsCore } from 'ngx-echarts';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(withInterceptors([authInterceptor, httpErrorInterceptor])),
    provideEchartsCore({ echarts: () => import('./echarts').then((m) => m.echarts) }),
  ],
};
