import { Component, computed, inject, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { LatencyPointResponse } from '../../../../models/metrics/metrics-response';
import { TASK_TYPE_LABELS, TaskType } from '../../../../models/tasks/task-type';
import { ThemeService } from '../../../../services/theme-service';
import { CHART_GRID, chartThemeOptions, formatMs, readChartColors } from '../metrics-chart';

const TASK_TYPES = Object.values(TaskType);

const avgName = (type: TaskType) => `${TASK_TYPE_LABELS[type]} avg`;
const p95Name = (type: TaskType) => `${TASK_TYPE_LABELS[type]} p95`;

@Component({
  selector: 'app-latency-chart',
  imports: [NgxEchartsDirective],
  template: `<div echarts class="chart" [options]="options" [merge]="data()"></div>`,
  styles: `
    .chart {
      height: 320px;
    }
  `,
})
export class LatencyChart {
  private readonly themeService = inject(ThemeService);

  readonly points = input.required<LatencyPointResponse[]>();
  readonly from = input.required<string>();
  readonly to = input.required<string>();

  protected readonly options: EChartsCoreOption = {
    tooltip: { trigger: 'axis', confine: true, valueFormatter: formatMs },
    legend: {
      type: 'scroll',
      top: 0,
      selected: Object.fromEntries(TASK_TYPES.map((type) => [p95Name(type), false])),
    },
    grid: { ...CHART_GRID, bottom: 64 },
    xAxis: { type: 'time' },
    yAxis: { type: 'value', axisLabel: { formatter: '{value} ms' } },
    dataZoom: [{ type: 'inside' }, { type: 'slider', height: 20, bottom: 12 }],
    series: TASK_TYPES.flatMap((type) => [
      { id: `${type}-avg`, name: avgName(type), type: 'line' },
      { id: `${type}-p95`, name: p95Name(type), type: 'line', lineStyle: { type: 'dashed' } },
    ]),
  };

  protected readonly data = computed<EChartsCoreOption>(() => {
    const points = this.points();
    const types = TASK_TYPES.filter((type) => points.some((p) => p.type === type));
    this.themeService.theme();
    const colors = readChartColors();
    const theme = chartThemeOptions(colors);
    return {
      ...theme,
      legend: { ...theme.legend, data: types.flatMap((type) => [avgName(type), p95Name(type)]) },
      xAxis: { ...theme.xAxis, min: this.from(), max: this.to() },
      dataZoom: [
        {},
        { textStyle: { color: colors.text }, borderColor: colors.axis, fillerColor: colors.grid },
      ],
      series: TASK_TYPES.flatMap((type) => {
        const ofType = points.filter((p) => p.type === type);
        const color = colors.taskTypes[type];
        return [
          { id: `${type}-avg`, color, data: ofType.map((p) => [p.bucketStart, p.avgMs]) },
          { id: `${type}-p95`, color, data: ofType.map((p) => [p.bucketStart, p.p95Ms]) },
        ];
      }),
    };
  });
}
