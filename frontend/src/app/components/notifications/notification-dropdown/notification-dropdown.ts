import { Component, ElementRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-notification-dropdown',
  imports: [RouterLink],
  templateUrl: './notification-dropdown.html',
  styleUrl: './notification-dropdown.scss',
  host: {
    '(document:click)': 'closeOnOutsideClick($event)',
  },
})
export class NotificationDropdown {
  readonly open = signal(false);
  readonly elementRef = inject(ElementRef);

  toggle() {
    this.open.update((value) => !value);
  }

  closeOnOutsideClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.open.set(false);
    }
  }
}
