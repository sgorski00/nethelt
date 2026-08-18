import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { MonitoringTasksService } from '../../../../services/monitoring-tasks-service';
import { TASK_TYPE_LABELS, TaskType } from '../../../../models/tasks/task-type';
import { MonitoringTaskCreateRequest } from '../../../../models/tasks/monitoring-task-request';

@Component({
  selector: 'app-create-task',
  imports: [ReactiveFormsModule],
  templateUrl: './create-task.html',
  styleUrl: './create-task.scss',
})
export class CreateTask {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(DialogRef);
  private readonly tasksService = inject(MonitoringTasksService);
  private readonly data = inject<{ deviceId: number }>(DIALOG_DATA);

  protected readonly taskTypes = Object.values(TaskType);
  protected readonly TaskType = TaskType;
  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;

  protected readonly errorMessage = signal('');

  protected readonly taskCreateForm = this.fb.nonNullable.group({
    intervalSeconds: [5, [Validators.required, Validators.min(1)]],
    type: [TaskType.PING, Validators.required],
    configuration: this.fb.nonNullable.group({
      port: [23, [Validators.required, Validators.min(1), Validators.max(65535)]],
      path: ['/health', Validators.required],
      timeoutSeconds: [2.0, [Validators.required, Validators.min(0.5), Validators.max(5)]],
    }),
  });

  protected submit(): void {
    if (this.taskCreateForm.invalid) {
      this.taskCreateForm.markAllAsTouched();
      return;
    }

    const value = this.taskCreateForm.getRawValue();

    let configuration;
    switch (value.type) {
      case TaskType.PING:
        configuration = {
          type: value.type,
          timeoutMs: value.configuration.timeoutSeconds * 1000,
        };
        break;

      case TaskType.TELNET:
        configuration = {
          type: value.type,
          port: value.configuration.port,
          timeoutMs: value.configuration.timeoutSeconds * 1000,
        };
        break;

      case TaskType.HTTP_HEALTHCHECK:
        configuration = {
          type: value.type,
          port: value.configuration.port,
          path: value.configuration.path,
          timeoutMs: value.configuration.timeoutSeconds * 1000,
        };
        break;
    }

    const request: MonitoringTaskCreateRequest = {
      type: value.type,
      intervalSeconds: value.intervalSeconds,
      configuration
    };

    this.tasksService.createTask(this.data.deviceId, request).subscribe({
      next: () => this.dialogRef.close({ created: true }),
      error: (err) =>
        this.errorMessage.set(
          err.error?.detail || 'An error occurred while creating the monitoring task.',
        ),
    });
  }
}
