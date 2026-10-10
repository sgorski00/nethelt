import { Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { LatencyPointResponse } from '../../../../models/metrics/metrics-response';
import { TASK_TYPE_LABELS, TaskType } from '../../../../models/tasks/task-type';
import { CHART_GRID, formatMs, TASK_TYPE_COLORS } from '../metrics-chart';

const TASK_TYPES = Object.values(TaskType);

const avgName = (type: TaskType) => `${TASK_TYPE_LABELS[type]} avg`;
const p95Name = (type: TaskType) => `${TASK_TYPE_LABELS[type]} p95`;

@Component({
  selector: 'app-latency-chart',
  imports: [NgxEchartsDirective],
  templateUrl: './latency-chart.html',
  styleUrl: './latency-chart.scss',
})
export class LatencyChart {
  readonly points = input.required<LatencyPointResponse[]>();
  readonly from = input.required<string>();
  readonly to = input.required<string>();

  protected readonly options: EChartsCoreOption = {
    tooltip: { trigger: 'axis', valueFormatter: formatMs },
    legend: {
      top: 0,
      selected: Object.fromEntries(TASK_TYPES.map((type) => [p95Name(type), false])),
    },
    grid: { ...CHART_GRID, bottom: 64 },
    xAxis: { type: 'time' },
    yAxis: { type: 'value', axisLabel: { formatter: '{value} ms' } },
    dataZoom: [{ type: 'inside' }, { type: 'slider', height: 20, bottom: 12 }],
    series: TASK_TYPES.flatMap((type) => [
      { id: `${type}-avg`, name: avgName(type), type: 'line', color: TASK_TYPE_COLORS[type] },
      {
        id: `${type}-p95`,
        name: p95Name(type),
        type: 'line',
        color: TASK_TYPE_COLORS[type],
        lineStyle: { type: 'dashed' },
      },
    ]),
  };

  protected readonly data = computed<EChartsCoreOption>(() => {
    const points = this.points();
    const types = TASK_TYPES.filter((type) => points.some((p) => p.type === type));
    return {
      legend: { data: types.flatMap((type) => [avgName(type), p95Name(type)]) },
      xAxis: { min: this.from(), max: this.to() },
      series: TASK_TYPES.flatMap((type) => {
        const ofType = points.filter((p) => p.type === type);
        return [
          { id: `${type}-avg`, data: ofType.map((p) => [p.bucketStart, p.avgMs]) },
          { id: `${type}-p95`, data: ofType.map((p) => [p.bucketStart, p.p95Ms]) },
        ];
      }),
    };
  });
}
