import { Component, input, output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TaskMetricsResponse } from '../../../../models/metrics/metrics-response';
import { TASK_TYPE_LABELS } from '../../../../models/tasks/task-type';
import { formatMs } from '../metrics-chart';

const HEALTHY_UPTIME_PERCENT = 99;

@Component({
  selector: 'app-metrics-breakdown',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './metrics-breakdown.html',
  styleUrl: './metrics-breakdown.scss',
})
export class MetricsBreakdown {
  readonly rows = input.required<TaskMetricsResponse[]>();
  readonly taskSelect = output<TaskMetricsResponse>();

  protected readonly TASK_TYPE_LABELS = TASK_TYPE_LABELS;
  protected readonly HEALTHY_UPTIME_PERCENT = HEALTHY_UPTIME_PERCENT;
  protected readonly formatMs = formatMs;
}
