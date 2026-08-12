import { Component, inject, signal } from '@angular/core';
import { DeviceService } from '../../../services/device-service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MonitoringTasksService } from '../../../services/monitoring-tasks-service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-tasks',
  imports: [],
  templateUrl: './tasks.html',
  styleUrl: './tasks.scss',
})
export class Tasks {
  private readonly deviceService = inject(DeviceService);
  private readonly tasksService = inject(MonitoringTasksService);

  protected readonly message = signal('');
  protected readonly selectedDeviceId = signal<number | ''>('');
  protected readonly devices = toSignal(this.deviceService.getDevicesList(), { initialValue: [] });
  protected readonly tasks = toSignal(
    toObservable(this.selectedDeviceId).pipe(
      switchMap((deviceId) => (deviceId !== '' ? this.tasksService.getTasks(deviceId) : [])),
    ),
    { initialValue: [] },
  );

  protected onDeviceChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    const deviceId = value !== '' ? Number(value) : '';
    this.selectedDeviceId.set(deviceId);
  }
}
