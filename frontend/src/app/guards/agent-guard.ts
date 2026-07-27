import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AgentService } from '../services/agent-service';
import { catchError, map, of } from 'rxjs';

export const hasAgentGuard: CanActivateFn = () => {
  const agentService = inject(AgentService);
  const router = inject(Router);

  return agentService.getAgent().pipe(
    map(() => true),
    catchError((err) => {
      if (err.status === 404) {
        return of(router.createUrlTree(['/console/agent/create']));
      } else {
        return of(router.createUrlTree(['/console']));
      }
    }),
  );
};

export const noAgentGuard: CanActivateFn = () => {
  const agentService = inject(AgentService);
  const router = inject(Router);

  return agentService.getAgent().pipe(
    map(() => router.createUrlTree(['/console/agent'])),
    catchError((err) => {
      if (err.status === 404) {
        return of(true);
      }

      return of(router.createUrlTree(['/console']));
    }),
  );
};
