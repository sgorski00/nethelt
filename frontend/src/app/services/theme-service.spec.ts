import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme-service';

describe('ThemeService', () => {
  let systemDark: boolean;

  const createService = () => TestBed.inject(ThemeService);
  const themeAttribute = () => document.documentElement.getAttribute('data-theme');

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    systemDark = false;
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({
        matches: systemDark,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    );
  });

  afterEach(() => vi.unstubAllGlobals());

  it('uses system preference by default', () => {
    systemDark = true;
    const service = createService();

    expect(service.preference()).toBe('system');
    expect(service.theme()).toBe('dark');
    expect(themeAttribute()).toBeNull();
  });

  it('restores stored preference', () => {
    localStorage.setItem('theme', 'dark');
    const service = createService();

    expect(service.theme()).toBe('dark');
    expect(themeAttribute()).toBe('dark');
  });

  it('applies and stores selected preference', () => {
    const service = createService();
    service.setPreference('light');

    expect(service.theme()).toBe('light');
    expect(themeAttribute()).toBe('light');
    expect(localStorage.getItem('theme')).toBe('light');
  });

  it('cycles through preferences', () => {
    const service = createService();

    service.cyclePreference();
    expect(service.preference()).toBe('light');
    service.cyclePreference();
    expect(service.preference()).toBe('dark');
    service.cyclePreference();
    expect(service.preference()).toBe('system');
  });
});
