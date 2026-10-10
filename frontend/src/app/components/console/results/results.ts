import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { of, switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { DeviceService } from '../../../services/device-service';
import { MonitoringTasksService } from '../../../services/monitoring-tasks-service';
import { MonitoringResultsService } from '../../../services/monitoring-results-service';
import { TASK_TYPE_LABELS, TaskType } from '../../../models/tasks/task-type';
import { Pagination } from '../../shared/pagination/pagination';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-results',
  imports: [DatePipe, Pagination],
  templateUrl: './results.html',
  styleUrl: './results.scss',
})
export class Results {
  private readonly route = inject(ActivatedRoute);
  private readonly deviceService = inject(DeviceService);
  private readonly tasksService = inject(MonitoringTasksService);
  private readonly resultsService = inject(MonitoringResultsService);

  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;
  protected readonly TaskType = TaskType;
  protected readonly pageSizes = [10, 20, 50, 100];

  protected readonly selectedDeviceId = signal<number | ''>(this.getNumberParam('deviceId'));
  protected readonly selectedTaskId = signal<number | ''>(this.getNumberParam('taskId'));
  protected readonly from = signal('');
  protected readonly to = signal('');
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly devices = toSignal(this.deviceService.getDevicesList(), { initialValue: [] });
  protected readonly tasks = toSignal(
    toObservable(this.selectedDeviceId).pipe(
      switchMap((deviceId) => (deviceId !== '' ? this.tasksService.getTasks(deviceId) : of([]))),
    ),
    { initialValue: [] },
  );
  protected readonly selectedTaskType = computed(
    () => this.tasks().find((task) => task.id === this.selectedTaskId())?.type,
  );
  protected readonly results = toSignal(
    toObservable(
      computed(() => ({
        deviceId: this.selectedDeviceId(),
        taskId: this.selectedTaskId(),
        from: this.from(),
        to: this.to(),
        page: this.page(),
        size: this.pageSize(),
      })),
    ).pipe(
      switchMap(({ deviceId, taskId, from, to, page, size }) =>
        deviceId !== '' && taskId !== ''
          ? this.resultsService.getResults(
              deviceId,
              taskId,
              this.toIsoString(from),
              this.toIsoString(to),
              page,
              size,
            )
          : of(undefined),
      ),
    ),
  );

  protected onDeviceChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedDeviceId.set(value !== '' ? Number(value) : '');
    this.selectedTaskId.set('');
    this.page.set(0);
  }

  protected onTaskChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedTaskId.set(value !== '' ? Number(value) : '');
    this.page.set(0);
  }

  protected onFromChange(event: Event): void {
    this.from.set((event.target as HTMLInputElement).value);
    this.page.set(0);
  }

  protected onToChange(event: Event): void {
    this.to.set((event.target as HTMLInputElement).value);
    this.page.set(0);
  }

  protected clearDates(): void {
    this.from.set('');
    this.to.set('');
    this.page.set(0);
  }

  protected onPageChange(page: number): void {
    this.page.set(page);
  }

  protected onPageSizeChange(event: Event): void {
    this.pageSize.set(Number((event.target as HTMLSelectElement).value));
    this.page.set(0);
  }

  private toIsoString(value: string): string | undefined {
    return value ? new Date(value).toISOString() : undefined;
  }

  private getNumberParam(name: string): number | '' {
    const value = this.route.snapshot.queryParamMap.get(name);
    return value ? Number(value) : '';
  }
}
