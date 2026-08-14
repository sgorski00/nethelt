import { Component, computed, inject, signal } from '@angular/core';
import { DeviceService } from '../../../services/device-service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MonitoringTasksService } from '../../../services/monitoring-tasks-service';
import { switchMap } from 'rxjs';
import { CreateTask } from './create-task/create-task';
import { Dialog } from '@angular/cdk/dialog';
import { DatePipe } from '@angular/common';
import { TASK_TYPE_LABELS } from '../../../models/tasks/task-type';
import { UpdateTask } from './update-task/update-task';
import { MonitoringTaskResponse } from '../../../models/tasks/monitoring-task-response';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-tasks',
  imports: [DatePipe],
  templateUrl: './tasks.html',
  styleUrl: './tasks.scss',
})
export class Tasks {
  private readonly deviceService = inject(DeviceService);
  private readonly tasksService = inject(MonitoringTasksService);
  private readonly dialog = inject(Dialog);

  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;

  private readonly reload = signal(0);
  protected readonly message = signal('');
  protected readonly errorMessage = signal('');
  protected readonly selectedDeviceId = signal<number | ''>('');
  protected readonly devices = toSignal(this.deviceService.getDevicesList(), { initialValue: [] });
  protected readonly tasks = toSignal(
    toObservable(
      computed(() => ({
        deviceId: this.selectedDeviceId(),
        reload: this.reload(),
      })),
    ).pipe(
      switchMap(({ deviceId }) => (deviceId !== '' ? this.tasksService.getTasks(deviceId) : [])),
    ),
    { initialValue: [] },
  );

  protected openCreateDialog() {
    const deviceId = this.selectedDeviceId();
    if (deviceId === '') return;
    const dialogRef = this.dialog.open(CreateTask, {
      data: { deviceId },
    });

    dialogRef.closed.subscribe((created) => {
      if (created) {
        this.message.set('Monitoring task created successfully.');
        this.reload.update((v) => v + 1);
      }
    });
  }

  protected openEditDialog(task: MonitoringTaskResponse) {
    const dialogRef = this.dialog.open(UpdateTask, {
      data: {
        deviceId: this.selectedDeviceId(),
        task: task,
      },
    });

    dialogRef.closed.subscribe((updated) => {
      if (updated) {
        this.message.set('Task updated successfully.');
        this.reload.update((v) => v + 1);
      }
    });
  }

  protected disable(id: number) {
    const deviceId = this.selectedDeviceId();
    if (deviceId === '') return;
    this.tasksService.disableTask(deviceId, id).subscribe({
      next: () => {
        this.message.set('Monitoring task disabled successfully.');
        this.reload.update((v) => v + 1);
      },
      error: (err) =>
        this.errorMessage.set(`Failed to disable monitoring task: ${err.error?.detail}`),
    });
  }

  protected enable(id: number) {
    const deviceId = this.selectedDeviceId();
    if (deviceId === '') return;
    this.tasksService.enableTask(deviceId, id).subscribe({
      next: () => {
        this.message.set('Monitoring task enabled successfully.');
        this.reload.update((v) => v + 1);
      },
      error: (err) =>
        this.errorMessage.set(`Failed to enable monitoring task: ${err.error?.detail}`),
    });
  }

  protected delete(task: MonitoringTaskResponse) {
    const deviceId = this.selectedDeviceId();
    if (deviceId === '') return;
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: `Delete Task #${task.id}`,
          message: 'Are you sure you want to delete this monitoring task?',
          confirmText: 'Delete',
        },
      })
      .closed.subscribe((confirmed) => {
        if (!confirmed) return;

        this.tasksService.deleteTask(deviceId, task.id).subscribe({
          next: () => {
            this.message.set('Monitoring task deleted successfully.');
            this.reload.update((v) => v + 1);
          },
          error: (err) =>
            this.errorMessage.set(`Failed to delete monitoring task : ${err.error?.detail}`),
        });
      });
  }

  protected onDeviceChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    const deviceId = value !== '' ? Number(value) : '';
    this.selectedDeviceId.set(deviceId);
  }
}
