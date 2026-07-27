import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AgentService } from '../../../../services/agent-service';
import { AgentCreateRequest } from '../../../../models/agent/agent-request';

@Component({
  selector: 'app-create-agent',
  imports: [ReactiveFormsModule],
  templateUrl: './create-agent.html',
  styleUrl: './create-agent.scss',
})
export class CreateAgent {
  private readonly fb = inject(FormBuilder);
  private readonly agentService = inject(AgentService);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal('');
  protected readonly agentCreateForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
  });

  protected submit(): void {
    if (this.agentCreateForm.invalid) {
      this.agentCreateForm.markAllAsTouched();
      return;
    }

    const request: AgentCreateRequest = this.agentCreateForm.getRawValue();

    this.agentService.create(request).subscribe({
      next: (res) => this.router.navigate(['/console/agent'], { state: { token: res.token } }),
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while creating the agent.'),
    });
  }
}
