import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { TASK_TYPE_LABELS, TaskType } from '../../../../models/tasks/task-type';
import { MonitoringTasksService } from '../../../../services/monitoring-tasks-service';
import { MonitoringTaskUpdateRequest } from '../../../../models/tasks/monitoring-task-request';
import {
  HttpHealthcheckMonitoringTaskResponse,
  PingMonitoringTaskResponse,
  TelnetMonitoringTaskResponse,
} from '../../../../models/tasks/monitoring-task-response';

interface UpdateTaskDialogData {
  deviceId: number;
  task:
    | PingMonitoringTaskResponse
    | TelnetMonitoringTaskResponse
    | HttpHealthcheckMonitoringTaskResponse;
}

@Component({
  selector: 'app-update-task',
  imports: [ReactiveFormsModule],
  templateUrl: './update-task.html',
  styleUrl: './update-task.scss',
})
export class UpdateTask {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(DialogRef);
  private readonly taskServices = inject(MonitoringTasksService);

  protected readonly data = inject<UpdateTaskDialogData>(DIALOG_DATA);

  protected readonly TaskType = TaskType;
  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;

  protected readonly errorMessage = signal('');
  protected readonly taskUpdateForm = this.fb.nonNullable.group({
    intervalSeconds: [
      this.intervalToSeconds(this.data.task.interval),
      [Validators.required, Validators.min(1)],
    ],
    configuration: this.fb.nonNullable.group({
      port: [23, [Validators.required, Validators.min(1), Validators.max(65535)]],
      path: ['/health', Validators.required],
      timeoutSeconds: [2.0, [Validators.required, Validators.min(0.5), Validators.max(5)]],
    }),
  });

  protected submit(): void {
    if (this.taskUpdateForm.invalid) {
      this.taskUpdateForm.markAllAsTouched();
      return;
    }

    const value = this.taskUpdateForm.getRawValue();

    let configuration;
    switch (this.data.task.type) {
      case TaskType.PING:
        configuration = {
          type: this.data.task.type,
          timeoutMs: value.configuration.timeoutSeconds * 1000,
        };
        break;

      case TaskType.TELNET:
        configuration = {
          type: this.data.task.type,
          port: value.configuration.port,
          timeoutMs: value.configuration.timeoutSeconds * 1000,
        };
        break;

      case TaskType.HTTP_HEALTHCHECK:
        configuration = {
          type: this.data.task.type,
          port: value.configuration.port,
          path: value.configuration.path,
          timeoutMs: value.configuration.timeoutSeconds * 1000,
        };
        break;
    }

    const request: MonitoringTaskUpdateRequest = {
      intervalSeconds: value.intervalSeconds,
      configuration,
    };

    this.taskServices.updateTask(this.data.deviceId, request, this.data.task.id).subscribe({
      next: () => this.dialogRef.close({ updated: true }),
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while updating the task.'),
    });
  }

  private intervalToSeconds(interval: string): number {
    const match = interval.match(/^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/);
    if (!match) return 5;
    const hours = Number(match[1] ?? 0);
    const minutes = Number(match[2] ?? 0);
    const seconds = Number(match[3] ?? 0);
    return hours * 3600 + minutes * 60 + seconds;
  }
}
