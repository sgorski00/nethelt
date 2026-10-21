import { Component, computed, inject, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { AvailabilityPointResponse } from '../../../../models/metrics/metrics-response';
import { ThemeService } from '../../../../services/theme-service';
import { CHART_GRID, chartThemeOptions, readChartColors } from '../metrics-chart';

@Component({
  selector: 'app-availability-chart',
  imports: [NgxEchartsDirective],
  template: `<div echarts class="chart" [options]="options" [merge]="data()"></div>`,
  styles: `
    .chart {
      height: 280px;
    }
  `,
})
export class AvailabilityChart {
  private readonly themeService = inject(ThemeService);

  readonly points = input.required<AvailabilityPointResponse[]>();
  readonly from = input.required<string>();
  readonly to = input.required<string>();
  readonly bucketSeconds = input.required<number>();

  protected readonly options: EChartsCoreOption = {
    tooltip: { trigger: 'axis', confine: true },
    legend: { type: 'scroll', top: 0 },
    grid: CHART_GRID,
    xAxis: { type: 'time' },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { id: 'successful', name: 'Successful', type: 'bar', stack: 'checks' },
      { id: 'failed', name: 'Failed', type: 'bar', stack: 'checks' },
    ],
  };

  protected readonly data = computed<EChartsCoreOption>(() => {
    const buckets = this.allBuckets();
    // The colors come from CSS tokens, so recompute them whenever the theme changes
    this.themeService.theme();
    const colors = readChartColors();
    const theme = chartThemeOptions(colors);
    return {
      ...theme,
      xAxis: { ...theme.xAxis, min: this.from(), max: this.to() },
      series: [
        {
          id: 'successful',
          color: colors.success,
          data: buckets.map(([start, p]) => [start, p?.successful ?? 0]),
        },
        {
          id: 'failed',
          color: colors.failure,
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
