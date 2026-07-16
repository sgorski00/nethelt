import { Component, inject, OnInit, signal } from '@angular/core';
import { NotificationService } from '../../../services/notification-service';
import { NotificationPreferencesResponse } from '../../../models/notifications/notification-preferences-response';

@Component({
  selector: 'app-notification-preferences',
  imports: [],
  templateUrl: './notification-preferences.html',
  styleUrl: './notification-preferences.scss',
})
export class NotificationPreferences implements OnInit {
  private readonly notificationService = inject(NotificationService);
  readonly preferences = signal<NotificationPreferencesResponse | null>(null);

  ngOnInit(): void {
    this.reloadPreferences();
  }

  private reloadPreferences() {
    this.notificationService.getPreferences().subscribe((res) => this.preferences.set(res));
  }
}
