import { Component, computed, ElementRef, inject, signal } from '@angular/core';
import { Dialog } from '@angular/cdk/dialog';
import { NotificationPreferencesDialog } from '../notification-preferences-dialog/notification-preferences-dialog';
import { NotificationService } from '../../../services/notification-service';
import { DatePipe } from '@angular/common';
import { combineLatest, switchMap } from 'rxjs';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-notification-dropdown',
  imports: [DatePipe],
  templateUrl: './notification-dropdown.html',
  styleUrl: './notification-dropdown.scss',
  host: {
    '(document:click)': 'closeOnOutsideClick($event)',
  },
})
export class NotificationDropdown {
  private readonly dialog = inject(Dialog);
  private readonly notificationService = inject(NotificationService);
  public readonly elementRef = inject(ElementRef);

  private readonly refresh = signal(0);
  private readonly currentPage = signal(0);
  public readonly showRead = signal(true);
  public readonly open = signal(false);
  public readonly notifications = computed(() => this.notificationsPage()?.content ?? []);

  public readonly unreadCount = toSignal(
    toObservable(this.refresh).pipe(switchMap(() => this.notificationService.getUnreadCount())),
    { initialValue: 0 },
  );
  private readonly notificationsPage = toSignal(
    combineLatest([
      toObservable(this.currentPage),
      toObservable(this.showRead),
      toObservable(this.refresh),
    ]).pipe(
      switchMap(([page, showRead]) => this.notificationService.getNotifications(page, showRead)),
    ),
    { initialValue: null },
  );
  public readonly hasNextPage = computed(() => !(this.notificationsPage()?.last ?? true));
  public readonly hasPreviousPage = computed(() => !(this.notificationsPage()?.first ?? true));

  openPreferencesDialog() {
    this.dialog.open(NotificationPreferencesDialog);
  }

  toggle() {
    this.open.update((value) => !value);
  }

  closeOnOutsideClick(event: MouseEvent) {
    const target = event.target as Node | null;
    if (!target || !this.elementRef.nativeElement.contains(target)) {
      this.open.set(false);
    }
  }

  nextPage() {
    this.currentPage.update((page) => page + 1);
    this.refresh.update((v) => v + 1);
  }

  previousPage() {
    this.currentPage.update((page) => page - 1);
    this.refresh.update((v) => v + 1);
  }

  changeFilter(showRead: boolean) {
    this.showRead.set(showRead);
    this.currentPage.set(0);
    this.refresh.update((v) => v + 1);
  }

  markAsRead(notificationId: number) {
    this.notificationService.markAsRead(notificationId).subscribe(() => {
      this.refresh.update((v) => v + 1);
    });
  }
}
