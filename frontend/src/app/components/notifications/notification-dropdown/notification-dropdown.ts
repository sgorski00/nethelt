import { Component, ElementRef, inject, signal } from '@angular/core';
import { Dialog } from '@angular/cdk/dialog';
import { NotificationPreferencesDialog } from '../notification-preferences-dialog/notification-preferences-dialog';
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
  private readonly dialog = inject(Dialog);

  public readonly open = signal(false);
  public readonly elementRef = inject(ElementRef);

  openPreferencesDialog() {
    this.dialog.open(NotificationPreferencesDialog);
  }

  toggle() {
    this.open.update((value) => !value);
  }

  closeOnOutsideClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.open.set(false);
    }
  }
}
