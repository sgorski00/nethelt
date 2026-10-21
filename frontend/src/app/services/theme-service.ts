import { computed, DestroyRef, DOCUMENT, inject, Injectable, signal } from '@angular/core';
import { Theme, ThemePreference } from '../models/theme/theme';

const PREFERENCES: ThemePreference[] = ['light', 'dark', 'system'];

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  // Also read by the inline script in index.html, which applies the theme before the app starts
  private static readonly THEME_KEY = 'theme';
  private readonly document = inject(DOCUMENT);
  private readonly darkQuery = this.document.defaultView?.matchMedia(
    '(prefers-color-scheme: dark)',
  );

  private readonly preferenceState = signal<ThemePreference>(this.loadPreference());
  private readonly systemDark = signal(this.darkQuery?.matches ?? false);

  public readonly preference = this.preferenceState.asReadonly();
  public readonly theme = computed<Theme>(() => {
    const preference = this.preferenceState();
    if (preference !== 'system') {
      return preference;
    }
    return this.systemDark() ? 'dark' : 'light';
  });

  constructor() {
    this.apply(this.preferenceState());
    const onChange = (event: MediaQueryListEvent) => this.systemDark.set(event.matches);
    this.darkQuery?.addEventListener('change', onChange);
    inject(DestroyRef).onDestroy(() => this.darkQuery?.removeEventListener('change', onChange));
  }

  public setPreference(preference: ThemePreference): void {
    this.apply(preference);
    this.preferenceState.set(preference);
    localStorage.setItem(ThemeService.THEME_KEY, preference);
  }

  public cyclePreference(): void {
    const next = (PREFERENCES.indexOf(this.preferenceState()) + 1) % PREFERENCES.length;
    this.setPreference(PREFERENCES[next]);
  }

  private apply(preference: ThemePreference): void {
    const root = this.document.documentElement;
    if (preference === 'system') {
      root.removeAttribute('data-theme');
    } else {
      root.setAttribute('data-theme', preference);
    }
  }

  private loadPreference(): ThemePreference {
    const stored = localStorage.getItem(ThemeService.THEME_KEY);
    return PREFERENCES.includes(stored as ThemePreference) ? (stored as ThemePreference) : 'system';
  }
}
