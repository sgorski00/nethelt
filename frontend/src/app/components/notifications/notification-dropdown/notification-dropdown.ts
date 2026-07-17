import { Component, computed, ElementRef, inject, OnInit, signal } from '@angular/core';
import { Dialog } from '@angular/cdk/dialog';
import { NotificationPreferencesDialog } from '../notification-preferences-dialog/notification-preferences-dialog';
import { NotificationService } from '../../../services/notification-service';
import { DatePipe } from '@angular/common';
import { PageResponse } from '../../../models/general/page-response';
import { NotificationResponse } from '../../../models/notifications/notification-response';

@Component({
  selector: 'app-notification-dropdown',
  imports: [DatePipe],
  templateUrl: './notification-dropdown.html',
  styleUrl: './notification-dropdown.scss',
  host: {
    '(document:click)': 'closeOnOutsideClick($event)',
  },
})
export class NotificationDropdown implements OnInit {
  private readonly dialog = inject(Dialog);
  private readonly notificationService = inject(NotificationService);
  public readonly elementRef = inject(ElementRef);

  private readonly currentPage = signal(0);
  private readonly notificationsPage = signal<PageResponse<NotificationResponse> | null>(null);

  public readonly showRead = signal(true);
  public readonly open = signal(false);
  public readonly notifications = computed(() => this.notificationsPage()?.content ?? []);
  public readonly unreadCount = signal(0);
  public readonly hasNextPage = computed(() => !(this.notificationsPage()?.last ?? true));
  public readonly hasPreviousPage = computed(() => !(this.notificationsPage()?.first ?? true));

  ngOnInit(): void {
    this.reloadNotifications();
  }

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

  nextPage() {
    this.currentPage.update((page) => page + 1);
    this.reloadNotifications();
  }

  previousPage() {
    this.currentPage.update((page) => page - 1);
    this.reloadNotifications();
  }

  changeFilter(showRead: boolean) {
    this.showRead.set(showRead);
    this.currentPage.set(0);
    this.reloadNotifications();
  }

  markAsRead(notificationId: number) {
    this.notificationService.markAsRead(notificationId).subscribe(() => {
      this.reloadNotifications();
    });
  }

  private reloadNotifications() {
    this.notificationService.getUnreadCount().subscribe((count) => this.unreadCount.set(count));
    this.notificationService
      .getNotifications(this.currentPage(), this.showRead())
      .subscribe((page) => this.notificationsPage.set(page));
  }
}
