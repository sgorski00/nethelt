import { Component, input, signal } from '@angular/core';

// Matches the `sm` breakpoint in styles/_breakpoints.scss
const PHONE_QUERY = '(max-width: 599.98px)';

let nextId = 0;

@Component({
  selector: 'app-filters-panel',
  imports: [],
  templateUrl: './filters-panel.html',
  styleUrl: './filters-panel.scss',
})
export class FiltersPanel {
  readonly title = input('Filters');
  readonly active = input(false);

  protected readonly bodyId = `filters-panel-${nextId++}`;
  // Collapsed by default on phones, where the filters would push the results off screen
  protected readonly open = signal(!window.matchMedia(PHONE_QUERY).matches);

  protected toggle() {
    this.open.update((open) => !open);
  }
}
