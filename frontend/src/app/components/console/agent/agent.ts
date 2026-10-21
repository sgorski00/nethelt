import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { concat, interval, last, Observable } from 'rxjs';
import { AgentService } from '../../../services/agent-service';
import { Dialog } from '@angular/cdk/dialog';
import { Router } from '@angular/router';
import { AgentResponse } from '../../../models/agent/agent-response';
import { DatePipe } from '@angular/common';
import { AgentStatus } from '../../../models/agent/agent-status';
import { AgentConnectionState } from '../../../models/agent/agent-connection-state';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

const STATUS_REFRESH_INTERVAL_MS = 30_000;
const MIN_UNAVAILABILITY_MINUTES = 2;
const MAX_UNAVAILABILITY_MINUTES = 1440;

@Component({
  selector: 'app-agent',
  imports: [DatePipe],
  templateUrl: './agent.html',
})
export class Agent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(Dialog);
  private readonly router = inject(Router);
  private readonly agentService = inject(AgentService);
  protected readonly AgentStatus = AgentStatus;
  protected readonly AgentConnectionState = AgentConnectionState;
  protected readonly MIN_UNAVAILABILITY_MINUTES = MIN_UNAVAILABILITY_MINUTES;
  protected readonly MAX_UNAVAILABILITY_MINUTES = MAX_UNAVAILABILITY_MINUTES;

  protected readonly errorMessage = signal<string>('');
  protected readonly agent = signal<AgentResponse | undefined>(undefined);

  protected readonly token = signal(this.router.currentNavigation()?.extras.state?.['token'] ?? '');
  protected readonly editing = signal(false);
  protected readonly name = signal('');
  protected readonly maxUnavailabilityMinutes = signal(0);
  protected readonly nameChanged = computed(() => this.agent()?.name !== this.name());
  protected readonly configurationChanged = computed(
    () =>
      this.agent()?.configuration.maxUnavailabilitySeconds !== this.maxUnavailabilityMinutes() * 60,
  );
  protected readonly maxUnavailabilityValid = computed(() => {
    const minutes = this.maxUnavailabilityMinutes();
    return (
      Number.isInteger(minutes) &&
      minutes >= MIN_UNAVAILABILITY_MINUTES &&
      minutes <= MAX_UNAVAILABILITY_MINUTES
    );
  });
  protected readonly changed = computed(
    () => this.agent() !== undefined && (this.nameChanged() || this.configurationChanged()),
  );

  ngOnInit(): void {
    this.reloadAgent();
    interval(STATUS_REFRESH_INTERVAL_MS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.refreshStatus());
  }

  protected edit() {
    const agent = this.agent();
    if (!agent) return;

    this.name.set(agent.name);
    this.maxUnavailabilityMinutes.set(agent.configuration.maxUnavailabilitySeconds / 60);
    this.editing.set(true);
  }

  protected cancelEdit() {
    this.editing.set(false);
  }

  protected save() {
    if (!this.agent() || !this.maxUnavailabilityValid()) return;

    const requests: Observable<AgentResponse>[] = [];
    if (this.nameChanged()) {
      requests.push(this.agentService.update({ name: this.name() }));
    }
    if (this.configurationChanged()) {
      requests.push(
        this.agentService.updateConfiguration({
          maxUnavailabilitySeconds: this.maxUnavailabilityMinutes() * 60,
        }),
      );
    }
    if (requests.length === 0) return;

    concat(...requests)
      .pipe(last())
      .subscribe({
        next: (updated) => {
          this.editing.set(false);
          this.agent.set(updated);
        },
        error: (err) => this.errorMessage.set(`Failed to update agent: ${err.error.detail}`),
      });
  }

  protected delete() {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Delete Agent',
          message: 'Are you sure you want to delete this agent?',
          confirmText: this.agent()!.name,
        },
      })
      .closed.subscribe((confirmed) => {
        if (!confirmed) return;

        this.agentService.delete().subscribe({
          next: () => this.router.navigate(['/console'], { queryParams: { agentDeleted: true } }),
          error: (err) =>
            this.errorMessage.set(
              err.error.detail || 'An error occurred while deleting the agent.',
            ),
        });
      });
  }

  protected changeStatus(status: AgentStatus) {
    this.agentService.changeStatus({ status: status }).subscribe({
      next: (updated) => this.agent.set(updated),
      error: (err) =>
        this.errorMessage.set(
          err.error.detail || 'An error occurred while changing the agent status.',
        ),
    });
  }

  protected renewToken() {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Renew Agent Token',
          message:
            'Are you sure you want to renew token? The current token will be invalidated and cannot be used anymore.',
          confirmText: 'Renew Token',
        },
      })
      .closed.subscribe((confirmed) => {
        if (!confirmed) return;

        this.agentService.renewToken().subscribe({
          next: (res) => this.token.set(res.token),
          error: (err) =>
            this.errorMessage.set(
              err.error?.detail || 'An error occurred while renewing the token.',
            ),
        });
      });
  }

  private refreshStatus() {
    this.agentService.getAgent().subscribe({
      next: (agent) => this.agent.set(agent),
    });
  }

  private reloadAgent() {
    this.agentService.getAgent().subscribe({
      next: (agent) => {
        this.agent.set(agent);
        this.name.set(agent.name);
      },
      error: (err) =>
        this.errorMessage.set(err.error.detail || 'An error occurred while loading the agent.'),
    });
  }
}
