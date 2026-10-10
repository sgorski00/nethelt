import { Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { AvailabilityPointResponse } from '../../../../models/metrics/metrics-response';
import { CHART_GRID, FAILURE_COLOR, SUCCESS_COLOR } from '../metrics-chart';

@Component({
  selector: 'app-availability-chart',
  imports: [NgxEchartsDirective],
  templateUrl: './availability-chart.html',
  styleUrl: './availability-chart.scss',
})
export class AvailabilityChart {
  readonly points = input.required<AvailabilityPointResponse[]>();
  readonly from = input.required<string>();
  readonly to = input.required<string>();
  readonly bucketSeconds = input.required<number>();

  protected readonly options: EChartsCoreOption = {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: CHART_GRID,
    xAxis: { type: 'time' },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { id: 'successful', name: 'Successful', type: 'bar', stack: 'checks', color: SUCCESS_COLOR },
      { id: 'failed', name: 'Failed', type: 'bar', stack: 'checks', color: FAILURE_COLOR },
    ],
  };

  protected readonly data = computed<EChartsCoreOption>(() => {
    const buckets = this.allBuckets();
    return {
      xAxis: { min: this.from(), max: this.to() },
      series: [
        { id: 'successful', data: buckets.map(([start, p]) => [start, p?.successful ?? 0]) },
        {
          id: 'failed',
          data: buckets.map(([start, p]) => [start, p ? p.total - p.successful : 0]),
        },
      ],
    };
  });

  private allBuckets(): [number, AvailabilityPointResponse | undefined][] {
    const bucketMs = this.bucketSeconds() * 1000;
    const byStart = new Map(this.points().map((p) => [Date.parse(p.bucketStart), p]));
    const end = Date.parse(this.to());
    const buckets: [number, AvailabilityPointResponse | undefined][] = [];
    for (
      let start = Math.floor(Date.parse(this.from()) / bucketMs) * bucketMs;
      start < end;
      start += bucketMs
    ) {
      buckets.push([start, byStart.get(start)]);
    }
    return buckets;
  }
}
