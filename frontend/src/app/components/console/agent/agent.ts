import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { AgentService } from '../../../services/agent-service';
import { Dialog } from '@angular/cdk/dialog';
import { Router } from '@angular/router';
import { AgentResponse } from '../../../models/agent/agent-response';
import { DatePipe } from '@angular/common';
import { AgentStatus } from '../../../models/agent/agent-status';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-agent',
  imports: [DatePipe],
  templateUrl: './agent.html',
  styleUrl: './agent.scss',
})
export class Agent implements OnInit {
  private readonly dialog = inject(Dialog);
  private readonly router = inject(Router);
  private readonly agentService = inject(AgentService);
  protected readonly AgentStatus = AgentStatus;

  protected readonly errorMessage = signal<string>('');
  protected readonly agent = signal<AgentResponse | undefined>(undefined);

  protected readonly token = signal(this.router.currentNavigation()?.extras.state?.['token'] ?? '');
  protected readonly editing = signal(false);
  protected readonly name = signal('');
  protected readonly changed = computed(() => {
    const agent = this.agent();
    if (!agent) return false;
    return agent.name !== this.name();
  });

  ngOnInit(): void {
    this.reloadAgent();
  }

  protected edit() {
    const agent = this.agent();
    if (!agent) return;

    this.name.set(agent.name);
    this.editing.set(true);
  }

  protected cancelEdit() {
    this.editing.set(false);
  }

  protected save() {
    const agent = this.agent();
    if (!agent) return;

    this.agentService
      .update({
        name: this.name(),
      })
      .subscribe({
        next: (updated) => {
          this.editing.set(false);
          this.agent.set(updated);
        },
        error: (err) => this.errorMessage.set(`Failed to update network: ${err.error.detail}`),
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
