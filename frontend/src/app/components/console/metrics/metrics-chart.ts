import { TaskType } from '../../../models/tasks/task-type';

export const CHART_GRID = { left: 56, right: 24, top: 40, bottom: 32 };

const FONT_FAMILY = "'Inter Variable', system-ui, sans-serif";

const TASK_TYPE_TOKENS: Record<TaskType, string> = {
  [TaskType.PING]: '--chart-ping',
  [TaskType.TELNET]: '--chart-telnet',
  [TaskType.HTTP_HEALTHCHECK]: '--chart-http',
};

export interface ChartColors {
  text: string;
  axis: string;
  grid: string;
  tooltipBg: string;
  tooltipBorder: string;
  success: string;
  failure: string;
  taskTypes: Record<TaskType, string>;
}

export function readChartColors(): ChartColors {
  const style = getComputedStyle(document.documentElement);
  const token = (name: string) => style.getPropertyValue(name).trim();
  return {
    text: token('--chart-text'),
    axis: token('--chart-axis'),
    grid: token('--chart-grid'),
    tooltipBg: token('--chart-tooltip-bg'),
    tooltipBorder: token('--chart-tooltip-border'),
    success: token('--chart-success'),
    failure: token('--chart-failure'),
    taskTypes: {
      [TaskType.PING]: token(TASK_TYPE_TOKENS[TaskType.PING]),
      [TaskType.TELNET]: token(TASK_TYPE_TOKENS[TaskType.TELNET]),
      [TaskType.HTTP_HEALTHCHECK]: token(TASK_TYPE_TOKENS[TaskType.HTTP_HEALTHCHECK]),
    },
  };
}

// Theme dependent parts of a chart with one x and one y axis
export function chartThemeOptions(colors: ChartColors) {
  const axis = {
    axisLine: { lineStyle: { color: colors.axis } },
    axisTick: { lineStyle: { color: colors.axis } },
    axisLabel: { color: colors.text },
    splitLine: { lineStyle: { color: colors.grid } },
  };
  return {
    textStyle: { fontFamily: FONT_FAMILY, color: colors.text },
    legend: {
      textStyle: { color: colors.text },
      pageTextStyle: { color: colors.text },
      pageIconColor: colors.text,
      pageIconInactiveColor: colors.axis,
      inactiveColor: colors.axis,
    },
    tooltip: {
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.text },
    },
    xAxis: axis,
    yAxis: axis,
  };
}

export function formatMs(value: unknown): string {
  return typeof value === 'number' ? `${Math.round(value)} ms` : '-';
}

export function formatInterval(seconds: number): string {
  if (seconds % 86_400 === 0) return seconds === 86_400 ? '1 day' : `${seconds / 86_400} days`;
  if (seconds % 3_600 === 0) return `${seconds / 3_600} h`;
  if (seconds % 60 === 0) return `${seconds / 60} min`;
  return `${seconds} s`;
}
