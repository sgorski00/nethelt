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
  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;

  protected readonly errorMessage = signal('');
  protected readonly taskCreateForm = this.fb.nonNullable.group({
    intervalSeconds: [5, [Validators.required, Validators.min(1)]],
    type: [TaskType.PING, Validators.required],
  });

  protected submit(): void {
    if (this.taskCreateForm.invalid) {
      this.taskCreateForm.markAllAsTouched();
      return;
    }

    const request: MonitoringTaskCreateRequest = this.taskCreateForm.getRawValue();

    this.tasksService.createTask(this.data.deviceId, request).subscribe({
      next: () => this.dialogRef.close({ created: true }),
      error: (err) =>
        this.errorMessage.set(
          err.error?.detail || 'An error occurred while creating the monitoring task.',
        ),
    });
  }
}
