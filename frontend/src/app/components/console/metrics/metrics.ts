import { Component, computed, DOCUMENT, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import {
  catchError,
  defer,
  EMPTY,
  finalize,
  fromEvent,
  interval,
  map,
  Observable,
  of,
  switchMap,
  tap,
  timer,
} from 'rxjs';
import { DeviceService } from '../../../services/device-service';
import { MonitoringTasksService } from '../../../services/monitoring-tasks-service';
import { MetricsService } from '../../../services/metrics-service';
import { TASK_TYPE_LABELS, TaskType } from '../../../models/tasks/task-type';
import { MetricsResponse } from '../../../models/metrics/metrics-response';
import { MetricsFilters } from '../../../models/metrics/metrics-filters';
import {
  METRICS_RANGE_LABELS,
  METRICS_RANGE_MS,
  MetricsRange,
} from '../../../models/metrics/metrics-range';

const REFRESH_INTERVAL_MS = 30_000;

interface MetricsQuery {
  deviceId?: number;
  taskId?: number;
  type?: TaskType;
  range: MetricsRange;
  from: string;
  to: string;
}

/** 'poll' refreshes periodically, 'once' fetches a fixed range, 'paused' waits for a visible tab. */
type FetchMode = 'poll' | 'once' | 'paused';

@Component({
  selector: 'app-metrics',
  imports: [DatePipe],
  templateUrl: './metrics.html',
  styleUrl: './metrics.scss',
})
export class Metrics {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);
  private readonly deviceService = inject(DeviceService);
  private readonly tasksService = inject(MonitoringTasksService);
  private readonly metricsService = inject(MetricsService);

  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;
  protected readonly METRICS_RANGE_LABELS = METRICS_RANGE_LABELS;
  protected readonly MetricsRange = MetricsRange;
  protected readonly taskTypes = Object.values(TaskType);
  protected readonly ranges = Object.values(MetricsRange);
  protected readonly refreshIntervalSeconds = REFRESH_INTERVAL_MS / 1000;

  protected readonly selectedDeviceId = signal(this.toNumber(this.queryParam('deviceId')));
  protected readonly selectedTaskId = signal(this.toNumber(this.queryParam('taskId')));
  protected readonly selectedType = signal(this.toOption(this.queryParam('type'), this.taskTypes));
  protected readonly range = signal(
    this.toOption(this.queryParam('range'), this.ranges) ?? MetricsRange.LAST_DAY,
  );
  protected readonly from = signal(this.queryParam('from') ?? '');
  protected readonly to = signal(this.queryParam('to') ?? '');
  protected readonly autoRefresh = signal(true);

  protected readonly devices = toSignal(this.deviceService.getDevicesList(), { initialValue: [] });
  protected readonly tasks = toSignal(
    toObservable(this.selectedDeviceId).pipe(
      switchMap((deviceId) =>
        deviceId !== undefined ? this.tasksService.getTasks(deviceId) : of([]),
      ),
    ),
    { initialValue: [] },
  );

  protected readonly metrics = signal<MetricsResponse | undefined>(undefined);
  protected readonly loading = signal(false);
  protected readonly error = signal(false);
  private readonly lastUpdatedAt = signal<number | null>(null);
  private readonly refreshRequests = signal(0);

  private readonly pageVisible = toSignal(
    fromEvent(this.document, 'visibilitychange').pipe(map(() => !this.document.hidden)),
    { initialValue: !this.document.hidden },
  );
  private readonly now = toSignal(interval(1000).pipe(map(() => Date.now())), {
    initialValue: Date.now(),
  });

  // A custom range keeps filling up until its end passes, so only a closed range stops polling.
  protected readonly canPoll = computed(() => {
    if (this.range() !== MetricsRange.CUSTOM) return true;
    const to = this.to();
    return !to || new Date(to).getTime() > this.now();
  });
  protected readonly secondsSinceUpdate = computed(() => {
    const updatedAt = this.lastUpdatedAt();
    return updatedAt !== null ? Math.max(0, Math.floor((this.now() - updatedAt) / 1000)) : null;
  });

  private readonly query = computed<MetricsQuery>(() => ({
    deviceId: this.selectedDeviceId(),
    taskId: this.selectedTaskId(),
    type: this.selectedType(),
    range: this.range(),
    from: this.from(),
    to: this.to(),
  }));

  private readonly fetchMode = computed<FetchMode>(() => {
    if (!this.canPoll() || !this.autoRefresh()) return 'once';
    return this.pageVisible() ? 'poll' : 'paused';
  });

  constructor() {
    toObservable(
      computed(() => ({
        query: this.query(),
        mode: this.fetchMode(),
        refresh: this.refreshRequests(),
      })),
    )
      .pipe(
        switchMap(({ query, mode }) => {
          switch (mode) {
            case 'paused':
              return EMPTY;
            case 'once':
              return this.fetch(query);
            case 'poll':
              return timer(0, REFRESH_INTERVAL_MS).pipe(switchMap(() => this.fetch(query)));
          }
        }),
        takeUntilDestroyed(),
      )
      .subscribe();

    effect(() => this.syncQueryParams(this.query()));
  }

  protected onDeviceChange(event: Event): void {
    this.selectedDeviceId.set(this.toNumber((event.target as HTMLSelectElement).value));
    this.selectedTaskId.set(undefined);
  }

  protected onTaskChange(event: Event): void {
    this.selectedTaskId.set(this.toNumber((event.target as HTMLSelectElement).value));
    this.selectedType.set(undefined);
  }

  protected onTypeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedType.set(this.toOption(value, this.taskTypes));
  }

  protected onRangeChange(event: Event): void {
    this.range.set((event.target as HTMLSelectElement).value as MetricsRange);
    this.from.set('');
    this.to.set('');
  }

  protected onFromChange(event: Event): void {
    this.from.set((event.target as HTMLInputElement).value);
  }

  protected onToChange(event: Event): void {
    this.to.set((event.target as HTMLInputElement).value);
  }

  protected onAutoRefreshChange(event: Event): void {
    this.autoRefresh.set((event.target as HTMLInputElement).checked);
  }

  protected refresh(): void {
    this.refreshRequests.update((value) => value + 1);
  }

  private fetch(query: MetricsQuery): Observable<MetricsResponse> {
    return defer(() => {
      this.loading.set(true);
      return this.metricsService.getMetrics(this.toFilters(query)).pipe(
        tap((metrics) => {
          this.metrics.set(metrics);
          this.error.set(false);
          this.lastUpdatedAt.set(Date.now());
        }),
        catchError(() => {
          this.error.set(true);
          return EMPTY;
        }),
        finalize(() => this.loading.set(false)),
      );
    });
  }

  private toFilters({ range, from, to, ...scope }: MetricsQuery): MetricsFilters {
    // Ranges are recomputed on every tick, so an open window slides with polling.
    const now = Date.now();
    if (range === MetricsRange.CUSTOM) {
      return {
        ...scope,
        from: from ? new Date(from).toISOString() : undefined,
        to: new Date(to || now).toISOString(),
      };
    }
    return {
      ...scope,
      from: new Date(now - METRICS_RANGE_MS[range]).toISOString(),
      to: new Date(now).toISOString(),
    };
  }

  // Router drops undefined params; from/to are only set for a custom range.
  private syncQueryParams({ range, from, to, ...scope }: MetricsQuery): void {
    this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: {
        ...scope,
        range: range !== MetricsRange.LAST_DAY ? range : undefined,
        from: from || undefined,
        to: to || undefined,
      },
    });
  }

  private queryParam(name: string): string | undefined {
    return this.route.snapshot.queryParamMap.get(name) ?? undefined;
  }

  private toNumber(value: string | undefined): number | undefined {
    return value ? Number(value) : undefined;
  }

  private toOption<T extends string>(
    value: string | undefined,
    options: readonly T[],
  ): T | undefined {
    return options.find((option) => option === value);
  }
}
