import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';
import { AgentService } from '../services/agent-service';
import { hasAgentGuard, noAgentGuard } from './agent-guard';

describe('agent guards', () => {
  const getAgent = vi.fn();

  beforeEach(() => {
    getAgent.mockReset();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AgentService, useValue: { getAgent } }],
    });
  });

  const run = (guard: CanActivateFn) =>
    firstValueFrom(
      TestBed.runInInjectionContext(
        () => guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot) as Observable<unknown>,
      ),
    );
  const agentFound = () => getAgent.mockReturnValue(of({}));
  const agentError = (status: number) =>
    getAgent.mockReturnValue(throwError(() => new HttpErrorResponse({ status })));

  describe('hasAgentGuard', () => {
    it('allows when agent exists', async () => {
      agentFound();
      expect(await run(hasAgentGuard)).toBe(true);
    });

    it('redirects to agent creation when agent does not exist', async () => {
      agentError(404);
      expect(((await run(hasAgentGuard)) as UrlTree).toString()).toBe('/console/agent/create');
    });

    it('redirects to console on other errors', async () => {
      agentError(500);
      expect(((await run(hasAgentGuard)) as UrlTree).toString()).toBe('/console');
    });
  });

  describe('noAgentGuard', () => {
    it('allows when agent does not exist', async () => {
      agentError(404);
      expect(await run(noAgentGuard)).toBe(true);
    });

    it('redirects to agent view when agent exists', async () => {
      agentFound();
      expect(((await run(noAgentGuard)) as UrlTree).toString()).toBe('/console/agent');
    });

    it('redirects to console on other errors', async () => {
      agentError(500);
      expect(((await run(noAgentGuard)) as UrlTree).toString()).toBe('/console');
    });
  });
});
